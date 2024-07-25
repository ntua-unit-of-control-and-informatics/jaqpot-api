package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.service.model.dto.PredictionRequestDto
import org.jaqpot.api.service.model.dto.PredictionResponseDto
import org.jaqpot.api.service.model.dto.legacy.LegacyDataEntryDto
import org.jaqpot.api.service.model.dto.legacy.LegacyDatasetDto
import org.jaqpot.api.service.model.dto.legacy.LegacyPredictionRequestDto
import org.jaqpot.api.service.runtime.RuntimeResolver
import org.springframework.http.HttpEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Service
class PredictionService(
    private val datasetRepository: DatasetRepository,
    private val runtimeResolver: RuntimeResolver,
    private val qsarToolboxPredictionService: QSARToolboxPredictionService
) {

    @Async
    fun executePredictionAndSaveResults(modelDto: PredictionModelDto, dataset: Dataset) {
        val datasetDto = dataset.toDto()


        val request = if (!modelDto.legacyPredictionService.isNullOrEmpty()) {
            val legacyPredictionRequestDto = generateLegacyPredictionRequest(modelDto, datasetDto)
            HttpEntity(
                legacyPredictionRequestDto
            )
        } else {
            val predictionRequestDto = PredictionRequestDto(
                modelDto,
                datasetDto,
            )
            HttpEntity(
                predictionRequestDto
            )
        }

        updateDatasetToExecuting(dataset)

        try {
            val results: List<Any> = makePredictionRequest(modelDto, request)
            storeDatasetSuccess(dataset, results)
        } catch (e: Exception) {
            logger.error(e) { "Prediction for dataset with id ${dataset.id} failed" }
            storeDatasetFailure(dataset, e)
        }
    }

    private fun generateLegacyPredictionRequest(
        modelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): LegacyPredictionRequestDto {
        val additionalInfo = mutableMapOf<String, Any>()
        additionalInfo["predictedFeatures"] = modelDto.dependentFeatures.map {
            it.key to it.name
        }.toMap()
        val legacyPredictionRequestDto = LegacyPredictionRequestDto(
            modelDto.rawModel,
            LegacyDatasetDto(LegacyDataEntryDto(datasetDto.input)),
            additionalInfo
        )
        return legacyPredictionRequestDto
    }

    private fun updateDatasetToExecuting(dataset: Dataset) {
        dataset.status = DatasetStatus.EXECUTING
        dataset.executedAt = OffsetDateTime.now()
        datasetRepository.save(dataset)
    }

    private fun storeDatasetSuccess(dataset: Dataset, results: List<Any>) {
        dataset.status = DatasetStatus.SUCCESS
        dataset.result = results
        dataset.executionFinishedAt = OffsetDateTime.now()
        datasetRepository.save(dataset)
    }

    private fun storeDatasetFailure(dataset: Dataset, err: Exception) {
        dataset.status = DatasetStatus.FAILURE
        dataset.failureReason = err.toString()

        datasetRepository.save(dataset)
    }

    private fun makePredictionRequest(
        modelDto: PredictionModelDto,
        request: HttpEntity<out Any>,
    ): List<Any> {
        if (modelDto.type == ModelDto.Type.QSAR_TOOLBOX) {
            return qsarToolboxPredictionService.makePredictionRequest(
                modelDto,
                request as HttpEntity<PredictionRequestDto>
            )
        }

        // uncomment to test request json
//        val objectMapper = ObjectMapper()
//        objectMapper.registerModule(JavaTimeModule())
//        val json = objectMapper.writeValueAsString(request)

        val restTemplate = RestTemplate()
        val inferenceUrl = runtimeResolver.resolveRuntimeUrl(modelDto)
        val response =
            restTemplate.postForEntity(inferenceUrl, request, PredictionResponseDto::class.java)

        val results: List<Any> = response.body?.predictions ?: emptyList()
        return results
    }
}

