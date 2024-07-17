package org.jaqpot.api.mapper

import org.jaqpot.api.entity.FeaturePossibleValue
import org.jaqpot.api.model.FeaturePossibleValueDto


fun FeaturePossibleValue.toDto(): FeaturePossibleValueDto {
    return FeaturePossibleValueDto(
        key = this.key,
        value = this.value
    )
}

fun FeaturePossibleValueDto.toEntity(): FeaturePossibleValue {
    return FeaturePossibleValue(
        key = this.key,
        value = this.value
    )
}

