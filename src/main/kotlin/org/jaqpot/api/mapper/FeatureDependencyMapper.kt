package org.jaqpot.api.mapper

import org.jaqpot.api.entity.FeatureDependency
import org.jaqpot.api.model.FeatureDto

fun FeatureDto.FeatureDependency.toEntity(): FeatureDependency {
    return when (this) {
        FeatureDto.FeatureDependency.DEPENDENT -> FeatureDependency.DEPENDENT
        FeatureDto.FeatureDependency.INDEPENDENT -> FeatureDependency.INDEPENDENT
    }
}

fun FeatureDependency.toDto(): FeatureDto.FeatureDependency {
    return when (this) {
        FeatureDependency.DEPENDENT -> FeatureDto.FeatureDependency.DEPENDENT
        FeatureDependency.INDEPENDENT -> FeatureDto.FeatureDependency.INDEPENDENT
    }
}

