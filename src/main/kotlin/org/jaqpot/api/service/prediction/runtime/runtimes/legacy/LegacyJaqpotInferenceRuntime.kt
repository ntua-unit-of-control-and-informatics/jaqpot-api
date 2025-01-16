package org.jaqpot.api.service.prediction.runtime.runtimes.legacy

import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.jaqpot.api.service.prediction.runtime.runtimes.RESTRuntime
import org.springframework.stereotype.Component

@Component
class LegacyJaqpotInferenceRuntime(private val runtimeConfiguration: RuntimeConfiguration) : RESTRuntime() {

    override fun getRuntimePath(predictionModelDto: PredictionModelDto): String {
        return "/predict/"
    }

    override fun getRuntimeUrl(predictionModelDto: PredictionModelDto): String {
        return runtimeConfiguration.legacyJaqpotInference
    }

    override fun createRequestBody(
        predictionModelDto: PredictionModelDto,
        datasetDto: DatasetDto
    ): Any {
        return generateLegacyPredictionRequest(predictionModelDto, datasetDto)
    }
}
