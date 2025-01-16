package org.jaqpot.api.service.prediction.runtime.runtimes.streaming

import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.asFlux
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.repository.DockerConfigRepository
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.jaqpot.api.service.prediction.runtime.runtimes.StreamingRuntime
import org.jaqpot.api.service.prediction.runtime.runtimes.util.DockerRuntimeUtil.Companion.retrieveDockerModelInferenceUrl
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux


@Service
class OpenAIModelRuntime(
    private val runtimeConfiguration: RuntimeConfiguration,
    private val dockerConfigRepository: DockerConfigRepository,

    ) : StreamingRuntime() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun sendStreamingPredictionRequest(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): Flux<String> {
        val host = OpenAIHost(
            baseUrl = "${getRuntimeUrl(predictionModelDto)}${getRuntimePath(predictionModelDto)}",
        )
        val openAIClient =
            OpenAI(
                OpenAIConfig(
                    host = host,
                    token = "EMPTY",
                )
            )
        val completions: Flow<ChatCompletionChunk> =
            openAIClient.chatCompletions(createRequestBody(predictionModelDto, datasetDto))
        return completions.asFlux().map { it.choices[0].delta?.content }
    }

    override fun getRuntimeUrl(predictionModelDto: PredictionModelDto): String {
        return retrieveDockerModelInferenceUrl(runtimeConfiguration, dockerConfigRepository, predictionModelDto)
    }

    override fun getRuntimePath(predictionModelDto: PredictionModelDto): String {
        return "/v1/"
    }

    override fun createRequestBody(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): ChatCompletionRequest {
        return ChatCompletionRequest(
            model = ModelId("mistralai/Mistral-7B-v0.3"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are a helpful assistant!"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = (datasetDto.input[0] as Map<String, String>)["prompt"]
                )
            )
        )
    }
}
