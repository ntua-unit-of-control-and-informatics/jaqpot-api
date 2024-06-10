package org.jaqpot.api.mapper

import org.jaqpot.api.entity.ModelVisibility
import org.jaqpot.api.model.ModelVisibilityDto

fun ModelVisibilityDto.toEntity(): ModelVisibility {
    return when (this) {
        ModelVisibilityDto.PUBLIC -> ModelVisibility.PUBLIC
        org.jaqpot.api.model.ModelVisibilityDto.ORG_SHARED -> ModelVisibility.ORG_SHARED
        org.jaqpot.api.model.ModelVisibilityDto.PRIVATE -> ModelVisibility.PRIVATE
    }
}

fun ModelVisibility.toDto(): ModelVisibilityDto {
    return when (this) {
        ModelVisibility.PUBLIC -> ModelVisibilityDto.PUBLIC
        ModelVisibility.ORG_SHARED -> ModelVisibilityDto.ORG_SHARED
        ModelVisibility.PRIVATE -> ModelVisibilityDto.PRIVATE
    }
}
