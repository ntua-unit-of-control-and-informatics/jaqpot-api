package org.jaqpot.api.service.prediction.util

import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.storage.StorageService
import java.time.OffsetDateTime

class PredictionUtil {

    companion object {
        fun updateDatasetToExecuting(
            dataset: Dataset,
            datasetRepository: DatasetRepository,
            storageService: StorageService
        ) {
            dataset.status = DatasetStatus.EXECUTING
            dataset.executedAt = OffsetDateTime.now()
            datasetRepository.save(dataset)
            if (storageService.storeRawDataset(dataset)) {
                datasetRepository.setDatasetInputAndResultToNull(dataset.id)
            }
        }

        fun storeDatasetSuccess(
            dataset: Dataset,
            results: List<Any>,
            datasetRepository: DatasetRepository,
            storageService: StorageService
        ) {
            dataset.status = DatasetStatus.SUCCESS
            dataset.result = results.toMutableList()
            dataset.executionFinishedAt = OffsetDateTime.now()
            datasetRepository.save(dataset)
            if (storageService.storeRawDataset(dataset)) {
                datasetRepository.setDatasetInputAndResultToNull(dataset.id)
            }
        }

        fun storeDatasetFailure(
            dataset: Dataset,
            err: Throwable,
            datasetRepository: DatasetRepository,
            storageService: StorageService
        ) {
            dataset.status = DatasetStatus.FAILURE
            dataset.failureReason = err.toString()
            dataset.executionFinishedAt = OffsetDateTime.now()

            datasetRepository.save(dataset)
            if (storageService.storeRawDataset(dataset)) {
                datasetRepository.setDatasetInputAndResultToNull(dataset.id)
            }
        }
    }

}
