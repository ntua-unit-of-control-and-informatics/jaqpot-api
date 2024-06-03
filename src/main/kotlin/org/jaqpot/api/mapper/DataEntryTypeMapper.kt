package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DataEntryType
import org.jaqpot.api.model.DataEntryDto

fun DataEntryDto.Type.toEntity(): DataEntryType {
    return when (this) {
        DataEntryDto.Type.ARRAY -> DataEntryType.ARRAY
    }
}

fun DataEntryType.toDto(): DataEntryDto.Type {
    return when (this) {
        DataEntryType.ARRAY -> DataEntryDto.Type.ARRAY
    }
}
