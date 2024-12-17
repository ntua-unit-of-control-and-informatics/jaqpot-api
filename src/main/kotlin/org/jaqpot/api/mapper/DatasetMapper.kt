package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetEntryType
import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DatasetCSVDto
import org.jaqpot.api.model.DatasetDto


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
        userId = this.userId,
        modelId = this.model.id,
        modelName = this.model.name,
        executedAt = this.executedAt,
        executionFinishedAt = this.executionFinishedAt,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
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
