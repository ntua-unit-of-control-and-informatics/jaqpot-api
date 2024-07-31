package org.jaqpot.api.mapper

import org.jaqpot.api.entity.ModelType
import org.jaqpot.api.model.ModelTypeDto

fun ModelTypeDto.toEntity(): ModelType {
    return when (this) {
        ModelTypeDto.SKLEARN -> ModelType.SKLEARN
        ModelTypeDto.TORCH -> ModelType.TORCH
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
        ModelTypeDto.QSAR_TOOLBOX -> ModelType.QSAR_TOOLBOX
    }
}

fun ModelType.toDto(): ModelTypeDto {
    return when (this) {
        ModelType.SKLEARN -> ModelTypeDto.SKLEARN
        ModelType.TORCH -> ModelTypeDto.TORCH
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
        ModelType.QSAR_TOOLBOX -> ModelTypeDto.QSAR_TOOLBOX
    }
}
