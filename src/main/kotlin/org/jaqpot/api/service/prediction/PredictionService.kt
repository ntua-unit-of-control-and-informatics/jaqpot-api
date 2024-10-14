package org.jaqpot.api.service.prediction

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.entity.Model
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toPredictionDto
import org.jaqpot.api.mapper.toPredictionModelDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.service.model.QSARToolboxPredictionService
import org.jaqpot.api.service.model.dto.PredictionResponseDto
import org.jaqpot.api.service.model.isQsarModel
import org.jaqpot.api.service.prediction.runtime.PredictionChain
import org.jaqpot.api.storage.StorageService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.OffsetDateTime


@Service
class PredictionService(
    private val datasetRepository: DatasetRepository,
    private val predictionChain: PredictionChain,
    private val storageService: StorageService,
    private val qsarToolboxPredictionService: QSARToolboxPredictionService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Async
    fun executePredictionAndSaveResults(model: Model, dataset: Dataset) {

        updateDatasetToExecuting(dataset)

        try {
            val results: List<Any> = makePredictionRequest(model, dataset)
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
        model: Model,
        dataset: Dataset
    ): List<Any> {
        val datasetDto = dataset.toDto()
        if (model.isQsarModel()) {
            return qsarToolboxPredictionService.makePredictionRequest(
                datasetDto,
                model.type
            )
        }

        val rawModel = storageService.readRawModel(model)
        val doaDtos = model.doas.map {
            val rawDoaData = storageService.readRawDoa(it)
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val doaData: Map<String, Any> = Gson().fromJson(rawDoaData.decodeToString(), type)
            it.toPredictionDto(doaData)
        }
        val predictionModelDto = model.toPredictionModelDto(rawModel, doaDtos)

        val response: PredictionResponseDto =
            predictionChain.getPredictionResults(predictionModelDto, datasetDto)

        val results: List<Any> = response.predictions ?: emptyList()
        return results
    }
}

