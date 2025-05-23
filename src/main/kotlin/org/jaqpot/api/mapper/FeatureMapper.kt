package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Feature
import org.jaqpot.api.entity.FeatureDependency
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.FeatureDto

fun Feature.toDto(): FeatureDto {
    return FeatureDto(
        key = this.key,
        name = this.name,
        featureType = this.featureType.toDto(),
        id = this.id,
        description = this.description,
        featureDependency = this.featureDependency.toDto(),
        possibleValues = this.possibleValues?.map { it.toDto() },
        units = this.units,
        range = this.range,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
}

fun FeatureDto.toEntity(model: Model, featureDependency: FeatureDependency): Feature {
    return Feature(
        id = this.id,
        model = model,
        key = this.key,
        name = this.name,
        description = this.description,
        featureDependency = featureDependency,
        featureType = this.featureType.toEntity(),
        possibleValues = this.possibleValues?.map { it.toEntity() },
        units = this.units,
        range = this.range
    )
}
