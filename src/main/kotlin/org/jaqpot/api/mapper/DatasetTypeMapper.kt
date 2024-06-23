package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DatasetType
import org.jaqpot.api.model.DatasetTypeDto

fun DatasetTypeDto.toEntity(): DatasetType {
    return when (this) {
        DatasetTypeDto.PREDICTION -> DatasetType.PREDICTION
    }
}

fun DatasetType.toDto(): DatasetTypeDto {
    return when (this) {
        DatasetType.PREDICTION -> DatasetTypeDto.PREDICTION
    }
}
