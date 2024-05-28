package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.ModelDto

fun Model.toDto(): ModelDto {
    return ModelDto(
        this.jaqpotpyVersion,
        this.libraries.map { it.toDto() },
        this.dependentFeatures.map { it.toDto() },
        this.independentFeatures.map { it.toDto() },
        byteArrayOf(), // returning empty byte array until https://github.com/OpenAPITools/openapi-generator/issues/17544 is fixed
        this.id,
        this.meta,
        this.public,
        this.type,
        this.reliability,
        this.pretrained,
        this.createdAt,
        this.updatedAt
    )
}

fun ModelDto.toEntity(): Model {
    return Model(
        this.id,
        this.meta,
        this.public,
        this.type,
        this.jaqpotpyVersion,
        this.libraries.map { l -> l.toEntity(this) },
        this.dependentFeatures.map { f -> f.toEntity(this) },
        this.independentFeatures.map { f -> f.toEntity(this) },
        this.reliability,
        this.pretrained,
        this.actualModel,
    )
}
