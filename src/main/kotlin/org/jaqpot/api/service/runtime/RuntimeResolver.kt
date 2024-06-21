package org.jaqpot.api.service.runtime

import org.jaqpot.api.entity.Model
import org.jaqpot.api.entity.ModelType
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.service.runtime.config.RuntimeProvider
import org.springframework.stereotype.Component
import java.util.*

@Component
class RuntimeResolver(
    val runtimeProvider: RuntimeProvider
) {

    fun resolveRuntimeUrl(model: Model): String {
        val legacyResolveRuntime = legacyResolveRuntime(model)

        return legacyResolveRuntime.orElse(
            when (model.type) {
                ModelType.R -> {
                    runtimeProvider.jaqpotRUrl
                }

                ModelType.SKLEARN -> {
                    runtimeProvider.jaqpotpyInferenceV6Url
                }

                else -> {
                    throw JaqpotRuntimeException("Model type is not yet supported")
                }
            }
        )
    }

    private fun legacyResolveRuntime(model: Model): Optional<String> {
        val legacyPredictionService = model.legacyPredictionService

        if (legacyPredictionService != null) {
            return Optional.of(resolveRuntimeWithPredictionService(legacyPredictionService, model))
        }

        return Optional.empty()
    }

    private fun resolveRuntimeWithPredictionService(legacyPredictionService: String, model: Model): String {
        if (legacyPredictionService.contains("jaqpot-r")) {
            return if (legacyPredictionService.contains("predict.pbpk")) {
                "${runtimeProvider.jaqpotRUrl}/ocpu/library/GenericR/R/predict.pbpk/json"
            } else if (legacyPredictionService.contains("jaqpot.predict.caret")) {
                "${runtimeProvider.jaqpotRUrl}/ocpu/library/GenericR/R/jaqpot.predict.caret/json"
            } else {
                throw JaqpotRuntimeException("unknown runtime with predictionService $legacyPredictionService")
            }
        } else if (legacyPredictionService.contains("jaqpot-inference.jaqpot")) {
            return runtimeProvider.jaqpotpyInferenceLegacyLatest
        } else if (legacyPredictionService.contains("jaqpot-python")) {
            return if (legacyPredictionService.contains("22")) {
                runtimeProvider.jaqpotpyInferenceLegacy22
            } else if (legacyPredictionService.contains("23")) {
                runtimeProvider.jaqpotpyInferenceLegacy23
            } else if (legacyPredictionService.contains("24")) {
                runtimeProvider.jaqpotpyInferenceLegacy24
            } else {
                runtimeProvider.jaqpotpyInferenceLegacyLatest
            }
        } else {
            throw JaqpotRuntimeException("unknown runtime with predictionService $legacyPredictionService")
        }
    }
}
