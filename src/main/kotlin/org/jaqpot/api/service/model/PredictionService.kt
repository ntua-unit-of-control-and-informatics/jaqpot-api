package org.jaqpot.api.service.model

import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.Model
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.repository.DatasetRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service


@Service
class PredictionService(
    private val datasetRepository: DatasetRepository,
) {

    fun createPrediction(model: Model, userId: String, datasetDto: DatasetDto): Dataset {
        val dataset = this.datasetRepository.save(datasetDto.toEntity(model, userId))
        this.sendPredictionToInference(dataset)

        return dataset
    }

    @Async
    fun sendPredictionToInference(dataset: Dataset) {

    }
}

