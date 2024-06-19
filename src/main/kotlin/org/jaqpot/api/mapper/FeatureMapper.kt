package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Feature
import org.jaqpot.api.entity.FeatureDependency
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.FeatureDto

fun Feature.toDto(): FeatureDto {
    return FeatureDto(
        this.name,
        this.featureType.toDto(),
        this.id,
        this.meta,
        this.description,
        this.featureDependency.toDto(),
        this.visible,
        this.possibleValues,
        this.createdAt,
        this.updatedAt,
    )
}

fun FeatureDto.toEntity(model: Model, featureDependency: FeatureDependency): Feature {
    return Feature(
        this.id,
        model,
        this.name,
        this.description,
        featureDependency,
        this.featureType.toEntity(),
        this.meta,
        this.visible,
        this.possibleValues,
    )
}
