package org.jaqpot.api.mapper

import org.jaqpot.api.entity.ModelType
import org.jaqpot.api.model.ModelDto

fun ModelDto.Type.toEntity(): ModelType {
    return when (this) {
        ModelDto.Type.SKLEARN -> ModelType.SKLEARN
        ModelDto.Type.TORCH -> ModelType.TORCH
        ModelDto.Type.R -> ModelType.R
    }
}

fun ModelType.toDto(): ModelDto.Type {
    return when (this) {
        ModelType.SKLEARN -> ModelDto.Type.SKLEARN
        ModelType.TORCH -> ModelDto.Type.TORCH
        ModelType.R -> ModelDto.Type.R
    }
}
