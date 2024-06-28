package org.jaqpot.api.service.runtime

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.service.runtime.config.RuntimeProvider
import org.springframework.stereotype.Component
import java.util.*

@Component
class RuntimeResolver(
    val runtimeProvider: RuntimeProvider
) {

    fun resolveRuntimeUrl(modelDto: PredictionModelDto): String {
        val legacyResolveRuntime = legacyResolveRuntime(modelDto)

        return legacyResolveRuntime.orElse(
            when (modelDto.type) {
                ModelDto.Type.R -> {
                    runtimeProvider.jaqpotRUrl
                }

                ModelDto.Type.SKLEARN -> {
                    runtimeProvider.jaqpotpyInferenceV6Url
                }

                else -> {
                    throw JaqpotRuntimeException("Model type is not yet supported")
                }
            }
        )
    }

    private fun legacyResolveRuntime(modelDto: PredictionModelDto): Optional<String> {
        val legacyPredictionService = modelDto.legacyPredictionService

        if (legacyPredictionService != null) {
            return Optional.of(resolveRuntimeWithPredictionService(legacyPredictionService))
        }

        return Optional.empty()
    }

    private fun resolveRuntimeWithPredictionService(legacyPredictionService: String): String {
        if (legacyPredictionService.contains("jaqpot-r")) {
            return if (legacyPredictionService.contains("predict.pbpk")) {
                "${runtimeProvider.jaqpotRUrl}/predict.pbpk"
            } else if (legacyPredictionService.contains("jaqpot.predict.caret")) {
                "${runtimeProvider.jaqpotRUrl}/predict.caret"
            } else {
                throw JaqpotRuntimeException("unknown runtime with predictionService $legacyPredictionService")
            }
        } else if (legacyPredictionService.contains("jaqpot-inference.jaqpot")) {
            return runtimeProvider.jaqpotpyInferenceV6Url
        } else if (legacyPredictionService.contains("jaqpot-python")) {
            return if (legacyPredictionService.contains("22")) {
                runtimeProvider.jaqpotpyInferenceLegacy22
            } else if (legacyPredictionService.contains("23")) {
                runtimeProvider.jaqpotpyInferenceLegacy23
            } else if (legacyPredictionService.contains("24")) {
                runtimeProvider.jaqpotpyInferenceLegacy24
            } else {
                runtimeProvider.jaqpotpyInferenceV6Url
//                throw JaqpotRuntimeException("unknown runtime with predictionService $legacyPredictionService")
            }
        } else {
            throw JaqpotRuntimeException("unknown runtime with predictionService $legacyPredictionService")
        }
    }
}
