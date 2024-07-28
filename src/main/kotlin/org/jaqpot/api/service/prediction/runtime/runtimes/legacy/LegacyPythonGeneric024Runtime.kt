package org.jaqpot.api.service.prediction.runtime.runtimes.legacy

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.jaqpot.api.service.prediction.runtime.runtimes.RuntimeBase
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component

@Component
class LegacyPythonGeneric024Runtime(private val runtimeConfiguration: RuntimeConfiguration) : RuntimeBase() {

    override fun getRuntimePath(predictionModelDto: PredictionModelDto): String {
        return getPathFromLegacyPredictionService(predictionModelDto.legacyPredictionService!!)
    }

    override fun getRuntimeUrl(): String {
        return runtimeConfiguration.legacyGenericPython24
    }

    override fun createRequestBody(predictionModelDto: PredictionModelDto, datasetDto: DatasetDto): HttpEntity<Any> {
        return HttpEntity(generateLegacyPredictionRequest(predictionModelDto, datasetDto))
    }
}
