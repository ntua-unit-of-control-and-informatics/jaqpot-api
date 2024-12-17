package org.jaqpot.api.service.prediction.runtime.runtimes.util

import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.repository.DockerConfigRepository
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class DockerRuntimeUtil {
    companion object {

        fun retrieveDockerModelInferenceUrl(
            runtimeConfiguration: RuntimeConfiguration,
            dockerConfigRepository: DockerConfigRepository,
            predictionModelDto: PredictionModelDto
        ): String {
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
    }
}
