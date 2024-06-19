package org.jaqpot.api.mapper

import org.jaqpot.api.entity.FeatureDependency
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.model.UserDto

fun Model.toDto(userDto: UserDto, userCanEdit: Boolean? = null): ModelDto {
    return ModelDto(
        this.name,
        this.type.toDto(),
        this.jaqpotpyVersion,
        this.libraries.map { it.toDto() },
        this.dependentFeatures.map { it.toDto() },
        this.independentFeatures.map { it.toDto() },
        this.visibility.toDto(),// returning empty byte array until https://github.com/OpenAPITools/openapi-generator/issues/17544 is fixed
        byteArrayOf(),
        this.id,
        this.meta,
        this.description,
        this.organizations.map { it.toDto() },
        this.pretrained,
        userDto,
        userCanEdit,
        this.createdAt,
        this.updatedAt,
    )
}

fun ModelDto.toEntity(creatorId: String): Model {
    val m = Model(
        this.id,
        null,
        creatorId,
        this.meta,
        this.name,
        this.description,
        this.type.toEntity(),
        this.jaqpotpyVersion,
        mutableListOf(),
        mutableListOf(),
        mutableListOf(),
        mutableSetOf(),
        this.visibility.toEntity(),
        null,
        this.pretrained,
        this.actualModel,
    )

    m.libraries.addAll(this.libraries.map { it -> it.toEntity(m) })
    m.dependentFeatures.addAll(this.dependentFeatures.map { it -> it.toEntity(m, FeatureDependency.DEPENDENT) })
    m.independentFeatures.addAll(this.independentFeatures.map { it -> it.toEntity(m, FeatureDependency.INDEPENDENT) })

    return m
}
