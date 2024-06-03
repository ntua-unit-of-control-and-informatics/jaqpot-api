package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DataEntry
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.model.DataEntryDto
import org.jaqpot.api.model.UserDto

fun DataEntry.toDto(userDto: UserDto): DataEntryDto {
    return DataEntryDto(
        this.type.toDto(),
        this.values,
        this.id,
        this.createdAt,
        this.updatedAt
    )
}

fun DataEntryDto.toEntity(dataset: Dataset, userId: String): DataEntry {
    return DataEntry(
        this.id,
        dataset,
        this.type.toEntity(),
        this.propertyValues,
    )
}
