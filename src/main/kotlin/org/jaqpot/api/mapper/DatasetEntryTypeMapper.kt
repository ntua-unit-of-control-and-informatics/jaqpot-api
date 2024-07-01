package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DatasetEntryType
import org.jaqpot.api.model.DatasetDto

fun DatasetDto.EntryType.toEntity(): DatasetEntryType {
    return when (this) {
        DatasetDto.EntryType.ARRAY -> DatasetEntryType.ARRAY
    }
}

fun DatasetEntryType.toDto(): DatasetDto.EntryType {
    return when (this) {
        DatasetEntryType.ARRAY -> DatasetDto.EntryType.ARRAY
    }
}
