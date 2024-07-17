package org.jaqpot.api.mapper

import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.entity.FeatureDependency
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.model.UserDto
import java.util.*

fun Model.toDto(userDto: UserDto? = null, userCanEdit: Boolean? = null, userCanDelete: Boolean? = null): ModelDto {
    return ModelDto(
        name = this.name,
        type = this.type.toDto(),
        jaqpotpyVersion = this.jaqpotpyVersion,
        libraries = this.libraries.map { it.toDto() },
        dependentFeatures = this.dependentFeatures.map { it.toDto() },
        independentFeatures = this.independentFeatures.map { it.toDto() },
        visibility = this.visibility.toDto(),
        actualModel = byteArrayOf(), // returning empty byte array until https://github.com/OpenAPITools/openapi-generator/issues/17544 is fixed
        id = this.id,
        meta = this.meta,
        description = this.description,
        organizations = this.organizations.map { it.toDto() },
        pretrained = this.pretrained,
        creator = userDto,
        canEdit = userCanEdit,
        canDelete = userCanDelete,
        tags = this.tags,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
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
        this.tags,
        // store as base64 representation
        Base64.getEncoder().encode(this.actualModel),
    )

    m.libraries.addAll(this.libraries.map { it -> it.toEntity(m) })
    m.dependentFeatures.addAll(this.dependentFeatures.map { it -> it.toEntity(m, FeatureDependency.DEPENDENT) })
    m.independentFeatures.addAll(this.independentFeatures.map { it -> it.toEntity(m, FeatureDependency.INDEPENDENT) })

    return m
}

fun Model.toPredictionModelDto(): PredictionModelDto {
    return PredictionModelDto(
        id = this.id,
        dependentFeatures = this.dependentFeatures.map { it.toDto() },
        independentFeatures = this.independentFeatures.map { it.toDto() },
        type = this.type.toDto(),
        rawModel = this.actualModel.decodeToString(),
        legacyAdditionalInfo = this.legacyAdditionalInfo,
        legacyPredictionService = this.legacyPredictionService
    )
}
