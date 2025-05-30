package org.jaqpot.api.service.prediction.runtime.runtimes.streaming

import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.model.PredictionRequestDto
import org.jaqpot.api.repository.DockerConfigRepository
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.jaqpot.api.service.prediction.runtime.runtimes.StreamingRuntime
import org.jaqpot.api.service.prediction.runtime.runtimes.util.DockerRuntimeUtil.Companion.retrieveDockerModelInferenceUrl
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.nio.charset.StandardCharsets

@Service
class JaqpotModelRuntime(
    private val runtimeConfiguration: RuntimeConfiguration,
    private val dockerConfigRepository: DockerConfigRepository,
) : StreamingRuntime() {

    companion object {
        const val SIXTEEN_MEGABYTES_IN_BYTES = 16 * 1024 * 1024
    }

    override fun sendStreamingPredictionRequest(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): Flux<String> {
//        val mockResponses = listOf(
//            "First chunk of response",
//            "Second chunk of response",
//            "Second chunk of response",
//            "Second chunk of response",
//            "Second chunk of response",
//            "Second chunk of response",
//            "Final chunk of response"
//        )
//
//        var output = ""
//
//        return Flux.fromIterable(mockResponses)
//            // Add artificial delay between emissions to simulate streaming
//            .delayElements(Duration.ofMillis(100))
//            .doOnSubscribe {
//                logger.info { "Starting mock streaming request for model ${predictionModelDto.id}" }
//            }
//            .doOnNext { response ->
//                logger.info { "Received mock chunk: $response" }
//                output += response
//            }
//            .doOnError { e ->
//                logger.error(e) { "Mock stream error for model ${predictionModelDto.id}: ${e.message}" }
//            }
//            .doFinally { signal ->
//                logger.info { "Mock stream finished with signal $signal for model ${predictionModelDto.id}" }
//                datasetService.addResultToDataset(datasetDto.id!!, mapOf("output" to mockResponses.joinToString(" ")))
//            }

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
    }

    override fun getRuntimeUrl(predictionModelDto: PredictionModelDto): String {
        return retrieveDockerModelInferenceUrl(runtimeConfiguration, dockerConfigRepository, predictionModelDto)
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

    override fun getRuntimePath(predictionModelDto: PredictionModelDto): String {
        return "/infer"
    }

}
