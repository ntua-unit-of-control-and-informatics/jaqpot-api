package org.jaqpot.api.mapper

import org.jaqpot.api.entity.FeatureType
import org.jaqpot.api.entity.FeatureType.DEPENDENT
import org.jaqpot.api.entity.FeatureType.INDEPENDENT
import org.jaqpot.api.model.FeatureDto

fun FeatureDto.FeatureType.toEntity(): FeatureType {
    return when (this) {
        FeatureDto.FeatureType.DEPENDENT -> DEPENDENT
        FeatureDto.FeatureType.INDEPENDENT -> INDEPENDENT
    }
}

fun FeatureType.toDto(): FeatureDto.FeatureType {
    return when (this) {
        DEPENDENT -> FeatureDto.FeatureType.DEPENDENT
        INDEPENDENT -> FeatureDto.FeatureType.INDEPENDENT
    }
}

