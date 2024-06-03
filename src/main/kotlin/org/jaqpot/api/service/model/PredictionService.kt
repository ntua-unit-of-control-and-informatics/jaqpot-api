package org.jaqpot.api.service.model

import org.jaqpot.api.entity.*
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.repository.DatasetRepository
import org.jaqpot.api.service.model.dto.PredictionRequestDto
import org.jaqpot.api.service.model.dto.PredictionResponseDto
import org.jaqpot.api.service.runtime.RuntimeResolver
import org.springframework.http.HttpEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*


@Service
class PredictionService(
    private val datasetRepository: DatasetRepository,
    private val runtimeResolver: RuntimeResolver
) {

    fun createAndPredictDataset(model: Model, userId: String, datasetDto: DatasetDto): Dataset {
        val dataset = this.datasetRepository.save(datasetDto.toEntity(model, userId))
        this.executePredictionAndSaveResults(model, dataset)

        return dataset
    }

    @Async
    fun executePredictionAndSaveResults(model: Model, dataset: Dataset) {
        val rawModel = Base64.getEncoder().encodeToString(model.actualModel)
        val request: HttpEntity<PredictionRequestDto> =
            HttpEntity(PredictionRequestDto(listOf(rawModel), dataset.toDto()))

        val results: List<Any> = makePredictionRequest(model, request)

        storeResults(dataset, results)
    }

    private fun storeResults(dataset: Dataset, results: List<Any>) {
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
        request: HttpEntity<PredictionRequestDto>
    ): List<Any> {
        val restTemplate = RestTemplate()
        val inferenceUrl = "${runtimeResolver.resolveRuntime(model)}/predict/"
        val response = restTemplate.postForEntity(inferenceUrl, request, PredictionResponseDto::class.java)

        val results: List<Any> = response.body?.predictions ?: emptyList()
        return results
    }
}

