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
    companion object {
        val R_RUNTIMES = mapOf(
            ModelDto.Type.R_BNLEARN_DISCRETE to "predict_bnlearn_discrete",
            ModelDto.Type.R_CARET to "predict_caret",
            ModelDto.Type.R_GBM to "predict_gbm",
            ModelDto.Type.R_NAIVE_BAYES to "predict_naive_bayess",
            ModelDto.Type.R_PBPK to "predict_pbpk",
            ModelDto.Type.R_RF to "predict_rf",
            ModelDto.Type.R_RPART to "predict_rpart",
            ModelDto.Type.R_SVM to "predict_svm",
            ModelDto.Type.R_TREE_CLASS to "predict_tree_class",
            ModelDto.Type.R_TREE_REGR to "predict_tree_regr",
        )
    }


    fun resolveRuntimeUrl(modelDto: PredictionModelDto): String {
        val legacyResolveRuntime = legacyResolveRuntime(modelDto)
        if (legacyResolveRuntime.isPresent) {
            return legacyResolveRuntime.get()
        }

        val rRuntime = resolveRRuntime(modelDto)
        if (rRuntime.isPresent) {
            return rRuntime.get()
        }

        return when (modelDto.type) {
            ModelDto.Type.SKLEARN -> {
                runtimeProvider.jaqpotpyInferenceV6Url
            }

            ModelDto.Type.TORCH -> {
                runtimeProvider.jaqpotpyInferenceV6Url
            }

            else -> {
                throw JaqpotRuntimeException("Model type is not yet supported")
            }
        }
    }

    private fun resolveRRuntime(modelDto: PredictionModelDto): Optional<String> {
        if (!R_RUNTIMES.contains(modelDto.type)) {
            return Optional.empty()
        }

        return Optional.of(R_RUNTIMES[modelDto.type]!!)
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
                "${runtimeProvider.jaqpotRUrl}/${R_RUNTIMES[ModelDto.Type.R_PBPK]}"
            } else if (legacyPredictionService.contains("predict.caret")) {
                "${runtimeProvider.jaqpotRUrl}/${R_RUNTIMES[ModelDto.Type.R_CARET]}"
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
