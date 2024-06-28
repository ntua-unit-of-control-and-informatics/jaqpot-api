package org.jaqpot.api.service.model

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetStatus
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
    fun executePredictionAndSaveResults(modelDto: PredictionModelDto, dataset: Dataset) {
        val datasetDto = dataset.toDto()

        val predictionRequestDto = PredictionRequestDto(
            modelDto,
            datasetDto,
        )
        val request: HttpEntity<PredictionRequestDto> =
            HttpEntity(
                predictionRequestDto
            )

        try {
            datasetRepository.updateStatus(dataset.id!!, DatasetStatus.EXECUTING)
            val results: List<Any> = makePredictionRequest(modelDto, request)
            storeDatasetSuccess(dataset, results)
        } catch (e: Exception) {
            logger.error(e) { "Prediction for dataset with id ${dataset.id} failed" }
            storeDatasetFailure(dataset, e)
        }
    }

    private fun storeDatasetFailure(dataset: Dataset, err: Exception) {
        dataset.status = DatasetStatus.FAILURE
        dataset.failureReason = err.toString()

        datasetRepository.save(dataset)
    }

    private fun storeDatasetSuccess(dataset: Dataset, results: List<Any>) {
        dataset.status = DatasetStatus.SUCCESS
        dataset.result = results
        datasetRepository.save(dataset)
    }

    private fun makePredictionRequest(
        modelDto: PredictionModelDto,
        request: HttpEntity<PredictionRequestDto>,
    ): List<Any> {
        val restTemplate = RestTemplate()
        val inferenceUrl = runtimeResolver.resolveRuntimeUrl(modelDto)
        val response =
            restTemplate.postForEntity(inferenceUrl, request, PredictionResponseDto::class.java)

        val results: List<Any> = response.body?.predictions ?: emptyList()
        return results
    }
}

