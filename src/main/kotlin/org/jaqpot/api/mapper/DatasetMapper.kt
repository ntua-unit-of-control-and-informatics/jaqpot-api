package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetEntryType
import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.AdminDatasetDto
import org.jaqpot.api.model.DatasetCSVDto
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.UserDto


fun Dataset.toDto(input: List<Any>, result: List<Any>?): DatasetDto {
    return DatasetDto(
        type = this.type.toDto(),
        id = this.id,
        entryType = this.entryType.toDto(),
        status = this.status.toDto(),
        name = this.name,
        failureReason = this.failureReason,
        input = input,
        result = result,
        resultTypes = this.resultTypes?.toDto(),
        userId = this.userId,
        modelId = this.model.id,
        modelName = this.model.name,
        executedAt = this.executedAt,
        executionFinishedAt = this.executionFinishedAt,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * Admin-only variant of [toDto]. The requesting user's details are attached
 * here so they are never exposed through the regular dataset endpoints.
 */
fun Dataset.toAdminDatasetDto(input: List<Any>, result: List<Any>?, user: UserDto?): AdminDatasetDto {
    val dto = toDto(input, result)
    return AdminDatasetDto(
        type = dto.type,
        entryType = AdminDatasetDto.EntryType.valueOf(dto.entryType.name),
        input = dto.input,
        id = dto.id,
        name = dto.name,
        result = dto.result,
        resultTypes = dto.resultTypes,
        status = dto.status?.let { AdminDatasetDto.Status.valueOf(it.name) },
        failureReason = dto.failureReason,
        userId = dto.userId,
        modelId = dto.modelId,
        modelName = dto.modelName,
        executedAt = dto.executedAt,
        executionFinishedAt = dto.executionFinishedAt,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
        user = user
    )
}

fun DatasetDto.toEntity(model: Model, userId: String, entryType: DatasetEntryType): Dataset {
    return Dataset(
        id = this.id,
        model = model,
        userId = userId,
        name = this.name,
        entryType = entryType,
        type = this.type.toEntity(),
        status = DatasetStatus.CREATED,
        input = this.input,
        resultTypes = this.resultTypes?.toEntity(),
        failureReason = this.failureReason
    )
}

fun DatasetCSVDto.toEntity(
    model: Model,
    userId: String,
    entryType: DatasetEntryType,
    input: List<Any>
): Dataset {
    return Dataset(
        id = this.id,
        model = model,
        userId = userId,
        entryType = entryType,
        type = this.type.toEntity(),
        status = DatasetStatus.CREATED,
        input = input,
        failureReason = this.failureReason
    )
}
