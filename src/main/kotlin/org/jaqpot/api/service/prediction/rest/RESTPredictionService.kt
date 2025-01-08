package org.jaqpot.api.service.prediction.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.model.PredictionResponseDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.service.prediction.runtime.PredictionChain
import org.jaqpot.api.service.prediction.util.PredictionUtil.Companion.storeDatasetFailure
import org.jaqpot.api.service.prediction.util.PredictionUtil.Companion.storeDatasetSuccess
import org.jaqpot.api.service.prediction.util.PredictionUtil.Companion.updateDatasetToExecuting
import org.jaqpot.api.storage.StorageService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service


@Service
class RESTPredictionService(
    private val datasetRepository: DatasetRepository,
    private val predictionChain: PredictionChain,
    private val storageService: StorageService,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Executes the prediction and saves the results.
     * <p>
     * Be careful with the Hibernate session here. The session is not getting transferred over
     * because of the @Async annotation. If there are any lazy model fields or dataset fields,
     * the session will break randomly.
     * </p>
     */
    @Async
    fun executePredictionAndSaveResults(predictionModelDto: PredictionModelDto, dataset: Dataset) {
        val datasetDto = dataset.toDto(dataset.input!!, dataset.result)

        updateDatasetToExecuting(dataset, datasetRepository, storageService)

        try {
            val response: PredictionResponseDto =
                predictionChain.getPredictionResults(predictionModelDto, datasetDto)

            val results: List<Any> = response.predictions
            storeDatasetSuccess(dataset, results, datasetRepository, storageService)
        } catch (e: Throwable) {
            logger.warn(e) { "Prediction failed for dataset with id ${dataset.id}" }
            storeDatasetFailure(dataset, e, datasetRepository, storageService)
        }
    }

}

