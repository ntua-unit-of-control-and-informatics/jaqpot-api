package org.jaqpot.api.mapper

import org.jaqpot.api.entity.FeaturePossibleValue
import org.jaqpot.api.model.FeaturePossibleValueDto


fun FeaturePossibleValue.toDto(): FeaturePossibleValueDto {
    return FeaturePossibleValueDto(
        value = this.value,
        description = this.description
    )
}

fun FeaturePossibleValueDto.toEntity(): FeaturePossibleValue {
    return FeaturePossibleValue(
        value = this.value,
        description = this.description
    )
}

