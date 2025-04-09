package org.jaqpot.api.mapper

import org.jaqpot.api.model.DatasetResultTypeDto

fun Map<String, DatasetResultTypeDto>.toEntity(): Map<String, String> {
    return this.mapValues { it.value.value } // enum to its string value (e.g., "BASE64")
}

fun Map<String, String>.toDto(): Map<String, DatasetResultTypeDto> {
    return this.mapValues { DatasetResultTypeDto.forValue(it.value) }
}
