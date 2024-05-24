package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Feature
import org.jaqpot.api.model.FeatureDto
import org.jaqpot.api.model.ModelDto

fun Feature.toDto(): FeatureDto {
    return FeatureDto(
        this.name,
        this.featureType.toDto(),
        this.id,
        this.featureType.toDto(),
        this.visible,
        this.createdAt,
        this.updatedAt
    )
}

fun FeatureDto.toEntity(modelDto: ModelDto): Feature {
    return Feature(
        this.id,
        modelDto.toEntity(),
        this.name,
        this.featureType.toEntity(),
        this.meta as Map<String, Any>,
        this.visible
    )
}
