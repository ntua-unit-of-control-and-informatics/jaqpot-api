package org.jaqpot.api.mapper

import org.jaqpot.api.entity.ModelTask
import org.jaqpot.api.model.ModelTaskDto

fun ModelTaskDto.toEntity(): ModelTask {
    return when (this) {
        ModelTaskDto.REGRESSION -> ModelTask.REGRESSION
        ModelTaskDto.BINARY_CLASSIFICATION -> ModelTask.BINARY_CLASSIFICATION
        ModelTaskDto.MULTICLASS_CLASSIFICATION -> ModelTask.MULTICLASS_CLASSIFICATION
    }
}

fun ModelTask.toDto(): ModelTaskDto {
    return when (this) {
        ModelTask.REGRESSION -> ModelTaskDto.REGRESSION
        ModelTask.BINARY_CLASSIFICATION -> ModelTaskDto.BINARY_CLASSIFICATION
        ModelTask.MULTICLASS_CLASSIFICATION -> ModelTaskDto.MULTICLASS_CLASSIFICATION
    }
}
