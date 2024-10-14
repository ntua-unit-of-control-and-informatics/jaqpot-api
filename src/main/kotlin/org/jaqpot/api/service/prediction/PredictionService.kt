package org.jaqpot.api.service.prediction

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.service.model.QSARToolboxPredictionService
import org.jaqpot.api.service.model.dto.PredictionResponseDto
import org.jaqpot.api.service.model.isQsarModel
import org.jaqpot.api.service.prediction.runtime.PredictionChain
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.OffsetDateTime


@Service
class PredictionService(
    private val datasetRepository: DatasetRepository,
    private val predictionChain: PredictionChain,
    private val qsarToolboxPredictionService: QSARToolboxPredictionService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Async
    fun executePredictionAndSaveResults(predictionModelDto: PredictionModelDto, dataset: Dataset) {
        val datasetDto = dataset.toDto()


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)


        updateDatasetToExecuting(dataset)

        try {
            val results: List<Any> = makePredictionRequest(predictionModelDto, datasetDto)
            storeDatasetSuccess(dataset, results)
        } catch (e: Exception) {
            logger.error(e) { "Prediction for dataset with id ${dataset.id} failed" }
            storeDatasetFailure(dataset, e)
        }
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
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): List<Any> {
        if (predictionModelDto.type.isQsarModel()) {
            return qsarToolboxPredictionService.makePredictionRequest(
                predictionModelDto,
                datasetDto,
                predictionModelDto.type
            )
        }

        val response: PredictionResponseDto =
            predictionChain.getPredictionResults(predictionModelDto, datasetDto)

        val results: List<Any> = response.predictions ?: emptyList()
        return results
    }
}

