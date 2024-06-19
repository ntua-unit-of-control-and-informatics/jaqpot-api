package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DataEntryRole
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DatasetDto


fun Dataset.toDto(): DatasetDto {
    return DatasetDto(
        this.type.toDto(),
        this.input.map { it.toDto() },
        this.id,
        this.input.map { it.toDto() },// copy input values to data entry field
        this.results.map { it.toDto() },
        this.status.toDto(),
        this.failureReason,
        this.createdAt,
        this.updatedAt
    )
}

fun DatasetDto.toEntity(model: Model, userId: String): Dataset {
    val d = Dataset(
        this.id,
        model,
        userId,
        this.type.toEntity(),
        mutableListOf(),
        DatasetStatus.CREATED,
        mutableListOf(),
        this.failureReason
    )

    d.input.addAll(this.input.map { it -> it.toEntity(d, DataEntryRole.INPUT) })
    d.results.addAll(this.results?.map { it -> it.toEntity(d, DataEntryRole.RESULTS) } ?: emptyList())

    return d
}
