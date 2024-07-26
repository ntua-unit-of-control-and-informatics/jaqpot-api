package org.jaqpot.api.service.prediction.runtime.runtimes

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.service.model.dto.PredictionRequestDto
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component

@Component
class JaqpotRV6Runtime(private val runtimeConfiguration: RuntimeConfiguration) : RuntimeBase() {
    companion object {
        val R_RUNTIME_PATHS = mapOf(
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

    override fun getRuntimeUrl(): String {
        return runtimeConfiguration.jaqpotRUrl
    }

    override fun getRuntimePath(predictionModelDto: PredictionModelDto): String {
        return "/${R_RUNTIME_PATHS[predictionModelDto.type]}"
    }

    override fun createRequestBody(predictionModelDto: PredictionModelDto, datasetDto: DatasetDto): HttpEntity<Any> {
        val predictionRequestDto = PredictionRequestDto(
            predictionModelDto,
            datasetDto,
        )
        return HttpEntity(
            predictionRequestDto
        )
    }
}
