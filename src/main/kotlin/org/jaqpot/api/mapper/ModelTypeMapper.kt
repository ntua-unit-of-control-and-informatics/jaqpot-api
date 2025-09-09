package org.jaqpot.api.mapper

import org.jaqpot.api.entity.ModelType
import org.jaqpot.api.model.ModelTypeDto

fun ModelTypeDto.toEntity(): ModelType {
    return when (this) {
        ModelTypeDto.SKLEARN_ONNX -> ModelType.SKLEARN_ONNX
        ModelTypeDto.TORCH_ONNX -> ModelType.TORCH_ONNX
        ModelTypeDto.TORCH_GEOMETRIC_ONNX -> ModelType.TORCH_GEOMETRIC_ONNX
        ModelTypeDto.TORCHSCRIPT -> ModelType.TORCHSCRIPT
        ModelTypeDto.R_BNLEARN_DISCRETE -> ModelType.R_BNLEARN_DISCRETE
        ModelTypeDto.R_CARET -> ModelType.R_CARET
        ModelTypeDto.R_GBM -> ModelType.R_GBM
        ModelTypeDto.R_NAIVE_BAYES -> ModelType.R_NAIVE_BAYES
        ModelTypeDto.R_PBPK -> ModelType.R_PBPK
        ModelTypeDto.R_RF -> ModelType.R_RF
        ModelTypeDto.R_RPART -> ModelType.R_RPART
        ModelTypeDto.R_SVM -> ModelType.R_SVM
        ModelTypeDto.R_TREE_CLASS -> ModelType.R_TREE_CLASS
        ModelTypeDto.R_TREE_REGR -> ModelType.R_TREE_REGR
        ModelTypeDto.QSAR_TOOLBOX_CALCULATOR -> ModelType.QSAR_TOOLBOX_CALCULATOR
        ModelTypeDto.QSAR_TOOLBOX_QSAR_MODEL -> ModelType.QSAR_TOOLBOX_QSAR_MODEL
        ModelTypeDto.QSAR_TOOLBOX_PROFILER -> ModelType.QSAR_TOOLBOX_PROFILER
        ModelTypeDto.DOCKER -> ModelType.DOCKER
        ModelTypeDto.OPENAI_LLM -> ModelType.OPENAI_LLM
        ModelTypeDto.CUSTOM_LLM -> ModelType.CUSTOM_LLM
    }
}

fun ModelType.toDto(): ModelTypeDto {
    return when (this) {
        ModelType.SKLEARN_ONNX -> ModelTypeDto.SKLEARN_ONNX
        ModelType.TORCH_ONNX -> ModelTypeDto.TORCH_ONNX
        ModelType.TORCH_GEOMETRIC_ONNX -> ModelTypeDto.TORCH_GEOMETRIC_ONNX
        ModelType.TORCHSCRIPT -> ModelTypeDto.TORCHSCRIPT
        ModelType.R_BNLEARN_DISCRETE -> ModelTypeDto.R_BNLEARN_DISCRETE
        ModelType.R_CARET -> ModelTypeDto.R_CARET
        ModelType.R_GBM -> ModelTypeDto.R_GBM
        ModelType.R_NAIVE_BAYES -> ModelTypeDto.R_NAIVE_BAYES
        ModelType.R_PBPK -> ModelTypeDto.R_PBPK
        ModelType.R_RF -> ModelTypeDto.R_RF
        ModelType.R_RPART -> ModelTypeDto.R_RPART
        ModelType.R_SVM -> ModelTypeDto.R_SVM
        ModelType.R_TREE_CLASS -> ModelTypeDto.R_TREE_CLASS
        ModelType.R_TREE_REGR -> ModelTypeDto.R_TREE_REGR
        ModelType.QSAR_TOOLBOX_CALCULATOR -> ModelTypeDto.QSAR_TOOLBOX_CALCULATOR
        ModelType.QSAR_TOOLBOX_QSAR_MODEL -> ModelTypeDto.QSAR_TOOLBOX_QSAR_MODEL
        ModelType.QSAR_TOOLBOX_PROFILER -> ModelTypeDto.QSAR_TOOLBOX_PROFILER
        ModelType.DOCKER -> ModelTypeDto.DOCKER
        ModelType.OPENAI_LLM -> ModelTypeDto.OPENAI_LLM
        ModelType.CUSTOM_LLM -> ModelTypeDto.CUSTOM_LLM
    }
}
