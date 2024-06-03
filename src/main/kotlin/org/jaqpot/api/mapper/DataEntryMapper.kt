package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DataEntry
import org.jaqpot.api.entity.DataEntryRole
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.model.DataEntryDto

fun DataEntry.toDto(): DataEntryDto {
    return DataEntryDto(
        this.type.toDto(),
        this.values,
        this.id,
        this.createdAt,
        this.updatedAt
    )
}

fun DataEntryDto.toEntity(dataset: Dataset, dataEntryRole: DataEntryRole): DataEntry {
    return DataEntry(
        this.id,
        dataset,
        this.type.toEntity(),
        dataEntryRole,
        this.propertyValues,
    )
}
