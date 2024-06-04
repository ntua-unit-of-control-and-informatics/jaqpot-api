package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DatasetType
import org.jaqpot.api.model.DatasetDto

fun DatasetDto.Type.toEntity(): DatasetType {
    return when (this) {
        DatasetDto.Type.PREDICTION -> DatasetType.PREDICTION
    }
}

fun DatasetType.toDto(): DatasetDto.Type {
    return when (this) {
        DatasetType.PREDICTION -> DatasetDto.Type.PREDICTION
    }
}
