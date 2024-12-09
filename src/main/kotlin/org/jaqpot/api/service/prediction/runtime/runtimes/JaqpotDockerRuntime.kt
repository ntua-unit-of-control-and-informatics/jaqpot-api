package org.jaqpot.api.service.prediction.runtime.runtimes

import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.model.PredictionRequestDto
import org.jaqpot.api.repository.DockerConfigRepository
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class JaqpotDockerRuntime(
    private val runtimeConfiguration: RuntimeConfiguration,
    private val dockerConfigRepository: DockerConfigRepository
) : RuntimeBase() {
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
