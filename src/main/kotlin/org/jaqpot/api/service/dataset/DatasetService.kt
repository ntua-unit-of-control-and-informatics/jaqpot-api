package org.jaqpot.api.service.dataset

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.jaqpot.api.DatasetApiDelegate
import org.jaqpot.api.entity.DatasetEntryType
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.mapper.toGetDatasets200ResponseDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.GetDatasets200ResponseDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.util.SortUtil.Companion.parseSortParameters
import org.jaqpot.api.storage.StorageService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

@Service
class DatasetService(
    private val datasetRepository: DatasetRepository,
    private val modelRepository: ModelRepository,
    private val authenticationFacade: AuthenticationFacade,
    private val storageService: StorageService
) : DatasetApiDelegate {
    companion object {
        const val DATASET_EXPIRATION_DAYS = 30L
        private const val RECONCILE_BATCH_SIZE = 200

        /**
         * Datasets younger than this are skipped by reconciliation so we never race with an
         * asynchronous offload that is still legitimately in flight.
         */
        private const val RECONCILE_MIN_AGE_MINUTES = 15L
        private val logger = KotlinLogging.logger {}
    }

    override fun createDataset(modelId: Long, datasetDto: DatasetDto): ResponseEntity<DatasetDto> {
        val userId = authenticationFacade.userId
        val model = modelRepository.findById(modelId).orElseThrow {
            throw JaqpotRuntimeException("Model with id ${datasetDto.modelId} not found")
        }
        val dataset = datasetDto.toEntity(model, userId, DatasetEntryType.ARRAY)
        datasetRepository.save(dataset)
        if (storageService.storeRawDataset(dataset)) {
            datasetRepository.setDatasetInputAndResultToNull(dataset.id)
        }

        return ResponseEntity.ok(dataset.toDto(dataset.input!!, dataset.result))
    }

    @PostAuthorize("@getDatasetAuthorizationLogic.decide(#root)")
    override fun getDatasetById(id: Long): ResponseEntity<DatasetDto> {
        val dataset = datasetRepository.findById(id)

        return dataset.map {
            val input = storageService.readRawDatasetInput(it)
            val result = if (it.shouldHaveResult()) {
                storageService.readRawDatasetResult(it)
            } else {
                null
            }
            ResponseEntity.ok(it.toDto(input, result))
        }
            .orElse(ResponseEntity.notFound().build())
    }

    override fun getDatasets(page: Int, size: Int, sort: List<String>?): ResponseEntity<GetDatasets200ResponseDto> {
        val userId = authenticationFacade.userId
        val pageable = PageRequest.of(page, size, Sort.by(parseSortParameters(sort)))
        val datasets = datasetRepository.findAllByUserId(userId, pageable)

        val inputsMap = storageService.readRawDatasetInputs(datasets.content)
        val resultsMap: MutableMap<String, List<Any>?> =
            datasets.content.associateBy { it.id.toString() }.mapValues { null }.toMutableMap()

        val datasetsWithResults = datasets.content.filter { it.shouldHaveResult() }
        storageService.readRawDatasetResults(datasetsWithResults).forEach {
            resultsMap[it.key] = it.value
        }

        return ResponseEntity.ok().body(datasets.toGetDatasets200ResponseDto(inputsMap, resultsMap))
    }

    override fun getDatasetsByModelId(
        modelId: Long,
        page: Int,
        size: Int,
        sort: List<String>?
    ): ResponseEntity<GetDatasets200ResponseDto> {
        val userId = authenticationFacade.userId
        val pageable = PageRequest.of(page, size, Sort.by(parseSortParameters(sort)))
        val datasets = datasetRepository.findAllByUserIdAndModelId(userId, modelId, pageable)

        return ResponseEntity.ok().body(datasets.toGetDatasets200ResponseDto(emptyMap(), emptyMap()))
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * *" /* every day at 3:00 AM */)
    fun purgeExpiredDatasets() {
        logger.info { "Purging expired datasets" }

        val expiredDatasets = datasetRepository.findAllByCreatedAtBefore(
            OffsetDateTime.now().minusDays(DATASET_EXPIRATION_DAYS)
        )
        datasetRepository.deleteAllById(expiredDatasets.mapNotNull { it.id })

        logger.info { "Purged ${expiredDatasets.size} expired datasets" }
    }

    /**
     * Recovers datasets whose input was persisted to the database but never offloaded to object
     * storage (e.g. because the asynchronous offload was dropped while the worker pool was saturated).
     * For each such dataset it stores the raw input/result in object storage and, on success, clears
     * the database copy — mirroring the offload performed on the prediction path.
     *
     * Intentionally not `@Transactional`: the object-storage I/O must not hold a database transaction
     * open, and [DatasetRepository.setDatasetInputAndResultToNull] runs in its own transaction.
     */
    @Scheduled(fixedDelay = 5, initialDelay = 2, timeUnit = TimeUnit.MINUTES)
    fun reconcileNonOffloadedDatasets() {
        val cutoff = OffsetDateTime.now().minusMinutes(RECONCILE_MIN_AGE_MINUTES)
        val datasets = datasetRepository.findDatasetsWithInputNotOffloaded(
            cutoff,
            PageRequest.of(0, RECONCILE_BATCH_SIZE, Sort.by("id").ascending())
        )

        if (datasets.isEmpty()) {
            return
        }

        logger.warn {
            "Reconciliation found ${datasets.size} dataset(s) with input not offloaded to storage " +
                "(older than $RECONCILE_MIN_AGE_MINUTES minutes); attempting to offload them now"
        }

        var offloaded = 0
        var failed = 0
        for (dataset in datasets) {
            try {
                if (storageService.storeRawDataset(dataset)) {
                    datasetRepository.setDatasetInputAndResultToNull(dataset.id)
                    offloaded++
                } else {
                    failed++
                }
            } catch (e: Exception) {
                failed++
                logger.error(e) { "Failed to reconcile dataset ${dataset.id}" }
            }
        }

        logger.warn {
            "Reconciliation finished: offloaded $offloaded dataset(s), $failed failure(s) " +
                "out of ${datasets.size} candidate(s)"
        }
    }
}
