package org.jaqpot.api.service.prediction.runtime.runtimes

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.ModelTypeDto
import org.jaqpot.api.service.model.dto.PredictionRequestDto
import org.jaqpot.api.service.prediction.runtime.config.RuntimeConfiguration
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component

@Component
class JaqpotRV6Runtime(private val runtimeConfiguration: RuntimeConfiguration) : RuntimeBase() {
    companion object {
        val R_RUNTIME_PATHS = mapOf(
            ModelTypeDto.R_BNLEARN_DISCRETE to "predict_bnlearn_discrete",
            ModelTypeDto.R_CARET to "predict_caret",
            ModelTypeDto.R_GBM to "predict_gbm",
            ModelTypeDto.R_NAIVE_BAYES to "predict_naive_bayess",
            ModelTypeDto.R_PBPK to "predict_pbpk",
            ModelTypeDto.R_RF to "predict_rf",
            ModelTypeDto.R_RPART to "predict_rpart",
            ModelTypeDto.R_SVM to "predict_svm",
            ModelTypeDto.R_TREE_CLASS to "predict_tree_class",
            ModelTypeDto.R_TREE_REGR to "predict_tree_regr",
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
