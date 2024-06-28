package org.jaqpot.api.mapper

import org.jaqpot.api.entity.FeatureType
import org.jaqpot.api.model.FeatureDto

fun FeatureDto.FeatureType.toEntity(): FeatureType {
    return when (this) {
        FeatureDto.FeatureType.INTEGER -> FeatureType.INTEGER
        FeatureDto.FeatureType.FLOAT -> FeatureType.FLOAT
        FeatureDto.FeatureType.CATEGORICAL -> FeatureType.CATEGORICAL
        FeatureDto.FeatureType.TEXT -> FeatureType.TEXT
    }
}

fun FeatureType.toDto(): FeatureDto.FeatureType {
    return when (this) {
        FeatureType.INTEGER -> FeatureDto.FeatureType.INTEGER
        FeatureType.FLOAT -> FeatureDto.FeatureType.FLOAT
        FeatureType.CATEGORICAL -> FeatureDto.FeatureType.CATEGORICAL
        FeatureType.TEXT -> FeatureDto.FeatureType.TEXT
    }
}

