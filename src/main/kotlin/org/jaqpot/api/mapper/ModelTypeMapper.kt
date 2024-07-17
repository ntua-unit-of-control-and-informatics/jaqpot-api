package org.jaqpot.api.mapper

import org.jaqpot.api.entity.ModelType
import org.jaqpot.api.model.ModelDto

fun ModelDto.Type.toEntity(): ModelType {
    return when (this) {
        ModelDto.Type.SKLEARN -> ModelType.SKLEARN
        ModelDto.Type.TORCH -> ModelType.TORCH
        ModelDto.Type.R_BNLEARN_DISCRETE -> ModelType.R_BNLEARN_DISCRETE
        ModelDto.Type.R_CARET -> ModelType.R_CARET
        ModelDto.Type.R_GBM -> ModelType.R_GBM
        ModelDto.Type.R_NAIVE_BAYES -> ModelType.R_NAIVE_BAYES
        ModelDto.Type.R_PBPK -> ModelType.R_PBPK
        ModelDto.Type.R_RF -> ModelType.R_RF
        ModelDto.Type.R_RPART -> ModelType.R_RPART
        ModelDto.Type.R_SVM -> ModelType.R_SVM
        ModelDto.Type.R_TREE_CLASS -> ModelType.R_TREE_CLASS
        ModelDto.Type.R_TREE_REGR -> ModelType.R_TREE_REGR
        ModelDto.Type.QSAR_TOOLBOX -> ModelType.QSAR_TOOLBOX
    }
}

fun ModelType.toDto(): ModelDto.Type {
    return when (this) {
        ModelType.SKLEARN -> ModelDto.Type.SKLEARN
        ModelType.TORCH -> ModelDto.Type.TORCH
        ModelType.R_BNLEARN_DISCRETE -> ModelDto.Type.R_BNLEARN_DISCRETE
        ModelType.R_CARET -> ModelDto.Type.R_CARET
        ModelType.R_GBM -> ModelDto.Type.R_GBM
        ModelType.R_NAIVE_BAYES -> ModelDto.Type.R_NAIVE_BAYES
        ModelType.R_PBPK -> ModelDto.Type.R_PBPK
        ModelType.R_RF -> ModelDto.Type.R_RF
        ModelType.R_RPART -> ModelDto.Type.R_RPART
        ModelType.R_SVM -> ModelDto.Type.R_SVM
        ModelType.R_TREE_CLASS -> ModelDto.Type.R_TREE_CLASS
        ModelType.R_TREE_REGR -> ModelDto.Type.R_TREE_REGR
        ModelType.QSAR_TOOLBOX -> ModelDto.Type.QSAR_TOOLBOX
    }
}
