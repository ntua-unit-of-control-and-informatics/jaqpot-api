package org.jaqpot.api.service.prediction.runtime.runtimes.legacy

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.jaqpot.api.service.prediction.runtime.runtimes.RuntimeBase
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component

@Component
class LegacyJaqpotInferenceRuntime(private val runtimeConfiguration: RuntimeConfiguration) : RuntimeBase() {

    override fun getRuntimePath(predictionModelDto: PredictionModelDto): String {
        return "/predict/"
    }

    override fun getRuntimeUrl(): String {
        return runtimeConfiguration.legacyJaqpotInference
    }

    override fun createRequestBody(predictionModelDto: PredictionModelDto, datasetDto: DatasetDto): HttpEntity<Any> {
        return HttpEntity(generateLegacyPredictionRequest(predictionModelDto, datasetDto))
    }
}
