package org.jaqpot.api.service.prediction.runtime.runtimes.rest

import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.model.PredictionRequestDto
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.jaqpot.api.service.prediction.runtime.runtimes.RESTRuntime
import org.springframework.stereotype.Component

@Component
class JaqpotPyV6Runtime(private val runtimeConfiguration: RuntimeConfiguration) : RESTRuntime() {
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
        return runtimeConfiguration.jaqpotpyInferenceV6Url
    }

    override fun getRuntimePath(predictionModelDto: PredictionModelDto): String {
        return "/predict"
    }

}
