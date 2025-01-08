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
            val jaqpotInternalServiceUrl = URI(runtimeConfiguration.jaqpotInternalServiceHost)
            val dockerConfigOptional = dockerConfigRepository.findByModelId(predictionModelDto.id)

            val inferenceUrl = if (dockerConfigOptional.isPresent) {
                val dockerConfig = dockerConfigOptional.get()
                UriComponentsBuilder.newInstance()
                    .scheme(jaqpotInternalServiceUrl.scheme)
                    .host("${dockerConfig.appName}.${jaqpotInternalServiceUrl.host}")
                    .build()
                    .toString()
            } else {
                jaqpotInternalServiceUrl.toString()
            }

            return inferenceUrl
        }
    }
}
