package org.jaqpot.api.mapper

import org.jaqpot.api.model.DatasetResultTypeDto

fun Map<String, Any>.toEntity(): Map<String, String> {
    return this.mapValues { DatasetResultTypeDto.forValue(it.value.toString()).value } // enum to its string value (e.g., "BASE64")
}

fun Map<String, String>.toDto(): Map<String, Any> {
    return this.mapValues { DatasetResultTypeDto.forValue(it.value) }
}
