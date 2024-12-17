package org.jaqpot.api.service.prediction.runtime.runtimes

import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.model.PredictionRequestDto
import org.jaqpot.api.repository.DockerConfigRepository
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.jaqpot.api.service.prediction.runtime.runtimes.util.DockerRuntimeUtil.Companion.retrieveDockerModelInferenceUrl
import org.jaqpot.api.service.prediction.runtime.runtimes.util.HttpClientUtil
import org.springframework.stereotype.Service
import reactor.netty.http.client.HttpClient

@Service
class JaqpotDockerModelRuntime(
    private val runtimeConfiguration: RuntimeConfiguration,
    private val dockerConfigRepository: DockerConfigRepository
) : RuntimeBase() {

    companion object {
        val dockerRuntimeHttpClient = HttpClientUtil.generateHttpClient(30, 30, 30, 30, 30)
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

    override fun getRuntimeUrl(predictionModelDto: PredictionModelDto): String {
        return retrieveDockerModelInferenceUrl(runtimeConfiguration, dockerConfigRepository, predictionModelDto)
    }

    override fun getRuntimePath(predictionModelDto: PredictionModelDto): String {
        return "/infer"
    }

    override fun getHttpClient(): HttpClient {
        return dockerRuntimeHttpClient
    }
}
