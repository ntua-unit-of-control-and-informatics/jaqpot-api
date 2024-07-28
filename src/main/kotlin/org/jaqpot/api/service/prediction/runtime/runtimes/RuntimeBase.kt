package org.jaqpot.api.service.prediction.runtime.runtimes

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.service.model.dto.PredictionResponseDto
import org.jaqpot.api.service.model.dto.legacy.LegacyDataEntryDto
import org.jaqpot.api.service.model.dto.legacy.LegacyDatasetDto
import org.jaqpot.api.service.model.dto.legacy.LegacyPredictionRequestDto
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.*

abstract class RuntimeBase {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    abstract fun getRuntimePath(predictionModelDto: PredictionModelDto): String

    abstract fun createRequestBody(predictionModelDto: PredictionModelDto, datasetDto: DatasetDto): HttpEntity<Any>

    abstract fun getRuntimeUrl(): String

    fun sendPredictionRequest(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): Optional<PredictionResponseDto> {
        val restTemplate = RestTemplate()
        val inferenceUrl = "${getRuntimeUrl()}${getRuntimePath(predictionModelDto)}"
        val request = createRequestBody(predictionModelDto, datasetDto)

        try {
            val response: ResponseEntity<PredictionResponseDto> =
                restTemplate.postForEntity(inferenceUrl, request, PredictionResponseDto::class.java)

            logger.info { "Prediction successful using ${getRuntimeUrl()} for model ${predictionModelDto.id}" }

            return Optional.of(response.body!!)
        } catch (e: Exception) {
            logger.warn(e) { "Prediction failed for ${getRuntimeUrl()} for model ${predictionModelDto.id}" }
            return Optional.empty()
        }
    }

    fun getPathFromLegacyPredictionService(legacyPredictionService: String): String {
        val legacyPredictionUrl = URI(legacyPredictionService).toURL()
        return legacyPredictionUrl.path
    }

    fun generateLegacyPredictionRequest(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): LegacyPredictionRequestDto {
        val additionalInfo = mutableMapOf<String, Any>(
            "predictedFeatures" to predictionModelDto.dependentFeatures.associate {
                it.key to it.name
            },
            "independentFeatures" to predictionModelDto.independentFeatures.associate {
                it.key to it.name
            }
        )
        additionalInfo["fromUser"] = mutableMapOf<String, Any>(
            "inputSeries" to predictionModelDto.independentFeatures.map {
                it.key
            }
        )

        val values: Map<Int, Any> =
            (0 until predictionModelDto.independentFeatures.size).associate { index ->
                index to (datasetDto.input[0] as Map<String, Any>).getValue(predictionModelDto.independentFeatures[index].key)
            }


        val legacyPredictionRequestDto = LegacyPredictionRequestDto(
            rawModel = arrayOf(predictionModelDto.rawModel),
            dataset = LegacyDatasetDto(
                LegacyDataEntryDto(values = values),
                features = predictionModelDto.independentFeatures.mapIndexed { index, it ->
                    mapOf(
                        "name" to it.name,
                        "key" to index
                    )
                }
            ),
            additionalInfo = additionalInfo
        )
        return legacyPredictionRequestDto
    }
}
