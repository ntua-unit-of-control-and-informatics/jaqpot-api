package org.jaqpot.api.service.runtime

import org.jaqpot.api.entity.Model
import org.jaqpot.api.entity.ModelType
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.service.runtime.config.RuntimeProvider
import org.springframework.stereotype.Component

@Component
class RuntimeResolver(
    val runtimeProvider: RuntimeProvider
) {

    fun resolveRuntimeUrl(model: Model): String {
        if (model.legacyPredictionService != null) {
            if (model.legacyPredictionService.contains("jaqpot-r")) {
                return if (model.legacyPredictionService.contains("predict.pbpk")) {
                    "${runtimeProvider.jaqpotRUrl}/ocpu/library/GenericR/R/predict.pbpk/json"
                } else if (model.legacyPredictionService.contains("jaqpot.predict.caret")) {
                    "${runtimeProvider.jaqpotRUrl}/ocpu/library/GenericR/R/jaqpot.predict.caret/json"
                } else {
                    throw JaqpotRuntimeException("unknown runtime with predictionService ${model.legacyPredictionService}")
                }
            } else if (model.legacyPredictionService.contains("jaqpot-python") || model.legacyPredictionService.contains(
                    "jaqpot-inference.jaqpot"
                )
            ) {
                return if (model.legacyPredictionService.contains("22")) {
                    runtimeProvider.jaqpotpyInferenceLegacy22
                } else if (model.legacyPredictionService.contains("23")) {
                    runtimeProvider.jaqpotpyInferenceLegacy23
                } else if (model.legacyPredictionService.contains("24")) {
                    runtimeProvider.jaqpotpyInferenceLegacy24
                } else {
                    throw JaqpotRuntimeException("unknown runtime with predictionService ${model.legacyPredictionService}")
                }
            } else {
                throw JaqpotRuntimeException("unknown runtime with predictionService ${model.legacyPredictionService}")
            }
        }

        return when (model.type) {
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
    }
}
