package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DataEntryRole
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DataEntryDto
import org.jaqpot.api.model.DatasetCSVDto
import org.jaqpot.api.model.DatasetDto


fun Dataset.toDto(): DatasetDto {
    return DatasetDto(
        this.type.toDto(),
        this.input!!.toDto(),
        this.id,
        this.results?.toDto(),
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
        null,
        DatasetStatus.CREATED,
        null,
        this.failureReason
    )

    d.input = this.input.toEntity(d, DataEntryRole.INPUT)
    d.results = this.results?.toEntity(d, DataEntryRole.RESULTS)

    return d
}

fun DatasetCSVDto.toEntity(model: Model, userId: String, input: DataEntryDto): Dataset {
    val d = Dataset(
        this.id,
        model,
        userId,
        this.type.toEntity(),
        null,
        DatasetStatus.CREATED,
        null,
        this.failureReason
    )

    d.input = input.toEntity(d, DataEntryRole.INPUT)

    return d
}
