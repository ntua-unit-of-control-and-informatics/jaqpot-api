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

fun ModelDto.toEntity(userId: String): Model {
    val m = Model(
        this.id,
        userId,
        this.meta,
        this.public,
        this.type,
        this.jaqpotpyVersion,
        mutableListOf(),
        mutableListOf(),
        mutableListOf(),
        this.reliability,
        this.pretrained,
        this.actualModel,
    )

    m.libraries.addAll(this.libraries.map { it -> it.toEntity(m) })
    m.dependentFeatures.addAll(this.dependentFeatures.map { it -> it.toEntity(m) })
    m.independentFeatures.addAll(this.independentFeatures.map { it -> it.toEntity(m) })

    return m
}
