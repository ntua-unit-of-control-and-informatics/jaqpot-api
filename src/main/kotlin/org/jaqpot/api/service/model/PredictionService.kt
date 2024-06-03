package org.jaqpot.api.service.model

import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.Model
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.repository.DatasetRepository
import org.springframework.http.HttpEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*


@Service
class PredictionService(
    private val datasetRepository: DatasetRepository,
) {

    fun createPrediction(model: Model, userId: String, datasetDto: DatasetDto): Dataset {
        val dataset = this.datasetRepository.save(datasetDto.toEntity(model, userId))
        this.sendPredictionToInference(model, dataset)

        return dataset
    }

    @Async
    fun sendPredictionToInference(model: Model, dataset: Dataset) {
        // TODO send request properly
        val restTemplate = RestTemplate()
        val fooResourceUrl = "http://localhost:8002/predict/"

        val rawModel = Base64.getEncoder().encodeToString(model.actualModel)
        val request: HttpEntity<PredictionRequestDto> =
            HttpEntity(PredictionRequestDto(listOf(rawModel), dataset))

        val response = restTemplate.postForEntity(fooResourceUrl, request, String::class.java)

        println(response)
    }
}

