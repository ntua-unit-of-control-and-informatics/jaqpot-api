package org.jaqpot.api.service.prediction.runtime.runtimes

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.model.PredictionResponseDto
import org.jaqpot.api.service.model.dto.legacy.LegacyDataEntryDto
import org.jaqpot.api.service.model.dto.legacy.LegacyDatasetDto
import org.jaqpot.api.service.model.dto.legacy.LegacyPredictionRequestDto
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import java.net.URI
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit


abstract class RESTRuntime : RuntimeBase() {
    companion object {
        private val logger = KotlinLogging.logger {}
        const val HUNDRED_MEGABYTES_IN_BYTES = 100 * 1024 * 1024
        private val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .responseTimeout(Duration.ofSeconds(60))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(60, TimeUnit.SECONDS))
                    .addHandlerLast(WriteTimeoutHandler(60, TimeUnit.SECONDS))
            }
    }

    fun sendPredictionRequest(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): Optional<PredictionResponseDto> = runBlocking {
        val client = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(getHttpClient()))
            .codecs { it.defaultCodecs().maxInMemorySize(HUNDRED_MEGABYTES_IN_BYTES) }
            .build()
        val inferenceUrl = "${getRuntimeUrl(predictionModelDto)}${getRuntimePath(predictionModelDto)}"

        try {
            // uncomment to test request json
//            val objectMapper = ObjectMapper()
//            objectMapper.registerModule(JavaTimeModule())
//            val json = objectMapper.writeValueAsString(request)
//            logger.info { json }

            val body = async {
                client.post()
                    .uri(inferenceUrl)
                    .bodyValue(createRequestBody(predictionModelDto, datasetDto))
                    .retrieve()
                    .awaitBody<PredictionResponseDto>()
            }

            val predictionResponseDto = body.await()

            logger.trace { "Prediction successful using ${getRuntimeUrl(predictionModelDto)} for model ${predictionModelDto.id}" }

            return@runBlocking Optional.of(predictionResponseDto)
        } catch (e: Exception) {
            if (e is WebClientResponseException.UnprocessableEntity)
                logger.warn(e) { "Prediction failed for ${getRuntimeUrl(predictionModelDto)} for model ${predictionModelDto.id} ${e.responseBodyAsString}" }
            else {
                logger.warn(e) { "Prediction failed for ${getRuntimeUrl(predictionModelDto)} for model ${predictionModelDto.id}" }
            }
            return@runBlocking Optional.empty()
        }
    }

    fun getPathFromLegacyPredictionService(legacyPredictionService: String): String {
        val legacyPredictionUrl = URI(legacyPredictionService).toURL()
        return legacyPredictionUrl.path
    }

    open fun getHttpClient(): HttpClient {
        return httpClient
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
            rawModel = arrayOf(predictionModelDto.rawModel!!),
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
