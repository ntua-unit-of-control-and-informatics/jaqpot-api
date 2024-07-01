package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Feature
import org.jaqpot.api.entity.FeatureDependency
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.FeatureDto

fun Feature.toDto(): FeatureDto {
    return FeatureDto(
        key = this.key,
        label = this.label,
        featureType = this.featureType.toDto(),
        id = this.id,
        meta = this.meta,
        description = this.description,
        featureDependency = this.featureDependency.toDto(),
        visible = this.visible,
        possibleValues = this.possibleValues,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
}

fun FeatureDto.toEntity(model: Model, featureDependency: FeatureDependency): Feature {
    return Feature(
        id = this.id,
        model = model,
        key = this.key,
        label = this.label,
        description = this.description,
        featureDependency = featureDependency,
        featureType = this.featureType.toEntity(),
        meta = this.meta,
        visible = this.visible,
        possibleValues = this.possibleValues,
    )
}
