package org.jaqpot.api.mapper

import org.jaqpot.api.entity.ModelVisibility
import org.jaqpot.api.model.ModelDto

fun ModelDto.Visibility.toEntity(): ModelVisibility {
    return when (this) {
        ModelDto.Visibility.PUBLIC -> ModelVisibility.PUBLIC
        ModelDto.Visibility.ORG_SHARED -> ModelVisibility.ORG_SHARED
        ModelDto.Visibility.PRIVATE -> ModelVisibility.PRIVATE
    }
}

fun ModelVisibility.toDto(): ModelDto.Visibility {
    return when (this) {
        ModelVisibility.PUBLIC -> ModelDto.Visibility.PUBLIC
        ModelVisibility.ORG_SHARED -> ModelDto.Visibility.ORG_SHARED
        ModelVisibility.PRIVATE -> ModelDto.Visibility.PRIVATE
    }
}
