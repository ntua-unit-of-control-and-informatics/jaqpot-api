package org.jaqpot.api.service.prediction.runtime.runtimes.streaming

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.model.PredictionRequestDto
import org.jaqpot.api.repository.DockerConfigRepository
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.jaqpot.api.service.prediction.runtime.runtimes.RuntimeBase
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import java.net.URI
import java.nio.charset.StandardCharsets

@Service
class StreamingRuntime(
    private val runtimeConfiguration: RuntimeConfiguration,
    private val dockerConfigRepository: DockerConfigRepository
) : RuntimeBase() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun sendStreamingPredictionRequest(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): Flux<String> {
        val webClient = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(getHttpClient()))
            .codecs { clientCodecConfigurer ->
                clientCodecConfigurer.defaultCodecs().apply {
                    maxInMemorySize(SIXTEEN_MEGABYTES_IN_BYTES)
                }
            }
            .build()

        val inferenceUrl = "${getRuntimeUrl(predictionModelDto)}${getRuntimePath(predictionModelDto)}"

        return webClient.post()
            .uri(inferenceUrl)
            .bodyValue(createRequestBody(predictionModelDto, datasetDto))
            .retrieve()
            .bodyToFlux(DataBuffer::class.java)
            .map { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                DataBufferUtils.release(dataBuffer)
                String(bytes, StandardCharsets.UTF_8)
            }
            .doOnSubscribe {
                logger.info { "Starting streaming request for model ${predictionModelDto.id}" }
            }
            .doOnNext { response ->
                logger.info { "Received chunk: $response" }
            }
            .doOnError { e ->
                logger.error(e) { "Stream error for model ${predictionModelDto.id}: ${e.message}" }
            }
            .doFinally { signal ->
                logger.info { "Stream finished with signal $signal for model ${predictionModelDto.id}" }
            }
    }

    override fun createRequestBody(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): Any {
        return PredictionRequestDto(
            predictionModelDto,
            datasetDto,
        )
    }

    // New abstract method for streaming request body
    override fun getRuntimeUrl(predictionModelDto: PredictionModelDto): String {
        val dockerUrlBase = URI(runtimeConfiguration.jaqpotDocker)
        val dockerConfigOptional = dockerConfigRepository.findByModelId(predictionModelDto.id)
        val inferenceUrl = if (dockerConfigOptional.isPresent) {
            val dockerConfig = dockerConfigOptional.get()
            UriComponentsBuilder.newInstance()
                .scheme(dockerUrlBase.scheme)
                .host("${dockerConfig.appName}.dockerUrlBase.host")
                .build()
                .toString()
        } else {
            dockerUrlBase.toString()
        }
        return inferenceUrl
    }

    override fun getRuntimePath(predictionModelDto: PredictionModelDto): String {
        return "/infer"
    }

}
