package org.jaqpot.api.service.prediction.runtime.runtimes

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.service.model.dto.PredictionRequestDto
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component

@Component
class JaqpotPyV6Runtime(private val runtimeConfiguration: RuntimeConfiguration) : RuntimeBase() {
    override fun createRequestBody(predictionModelDto: PredictionModelDto, datasetDto: DatasetDto): HttpEntity<Any> {
        val predictionRequestDto = PredictionRequestDto(
            predictionModelDto,
            datasetDto,
        )
        return HttpEntity(
            predictionRequestDto
        )
    }

    override fun getRuntimeUrl(): String {
        return runtimeConfiguration.jaqpotpyInferenceV6Url
    }

}
