package org.jaqpot.api.mapper

import org.jaqpot.api.entity.FeatureType
import org.jaqpot.api.model.FeatureDto

fun FeatureDto.FeatureType.toEntity(): FeatureType {
    return when (this) {
        FeatureDto.FeatureType.NUMERICAL -> FeatureType.NUMERICAL
        FeatureDto.FeatureType.CATEGORICAL -> FeatureType.CATEGORICAL
    }
}

fun FeatureType.toDto(): FeatureDto.FeatureType {
    return when (this) {
        FeatureType.NUMERICAL -> FeatureDto.FeatureType.NUMERICAL
        FeatureType.CATEGORICAL -> FeatureDto.FeatureType.CATEGORICAL
    }
}

