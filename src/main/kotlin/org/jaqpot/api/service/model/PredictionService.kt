package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.*
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.service.model.dto.PredictionRequestDto
import org.jaqpot.api.service.model.dto.PredictionResponseDto
import org.jaqpot.api.service.runtime.RuntimeResolver
import org.springframework.http.HttpEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

private val logger = KotlinLogging.logger {}

@Service
class PredictionService(
    private val datasetRepository: DatasetRepository,
    private val runtimeResolver: RuntimeResolver
) {

    @Async
    fun executePredictionAndSaveResults(model: Model, dataset: Dataset) {
        val datasetDto = dataset.toDto()

        val predictionRequestDto = PredictionRequestDto(
            model.toDto(null, null),
            datasetDto,
            model.legacyAdditionalInfo,
            model.actualModel.decodeToString()
        )
        val request: HttpEntity<PredictionRequestDto> =
            HttpEntity(
                predictionRequestDto
            )

        try {
            datasetRepository.updateStatus(dataset.id!!, DatasetStatus.EXECUTING)
            val results: List<Any> = makePredictionRequest(model, request)
            storeDatasetSuccess(dataset, results)
        } catch (err: Exception) {
            logger.error(err) { "Prediction for dataset with id ${dataset.id} failed" }
            storeDatasetFailure(dataset, err)
        }
    }

    private fun storeDatasetFailure(dataset: Dataset, err: Exception) {
        dataset.status = DatasetStatus.FAILURE
        dataset.failureReason = err.toString()

        datasetRepository.save(dataset)
    }

    private fun storeDatasetSuccess(dataset: Dataset, results: List<Any>) {
        dataset.status = DatasetStatus.SUCCESS
        dataset.results.clear()
        dataset.results.addAll(
            listOf(
                DataEntry(
                    null,
                    dataset,
                    DataEntryType.ARRAY,
                    DataEntryRole.RESULTS,
                    results
                )
            )
        )

        datasetRepository.save(dataset)
    }

    private fun makePredictionRequest(
        model: Model,
        request: HttpEntity<PredictionRequestDto>,
    ): List<Any> {
        val restTemplate = RestTemplate()
        val inferenceUrl = runtimeResolver.resolveRuntimeUrl(model)
        val response =
            restTemplate.postForEntity(inferenceUrl, request, PredictionResponseDto::class.java)

        val results: List<Any> = response.body?.predictions ?: emptyList()
        return results
    }
}

