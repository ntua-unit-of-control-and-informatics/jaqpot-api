package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Feature
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.FeatureDto

fun Feature.toDto(): FeatureDto {
    return FeatureDto(
        this.name,
        this.featureType.toDto(),
        this.id,
        this.meta,
        this.visible,
        this.createdAt,
        this.updatedAt
    )
}

fun FeatureDto.toEntity(model: Model): Feature {
    return Feature(
        this.id,
        model,
        this.name,
        this.featureType.toEntity(),
        this.meta,
        this.visible,
    )
}
