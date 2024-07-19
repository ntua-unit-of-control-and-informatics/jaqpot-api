package org.jaqpot.api.mapper

import org.jaqpot.api.entity.OrganizationVisibility
import org.jaqpot.api.model.OrganizationVisibilityDto

fun OrganizationVisibilityDto.toEntity(): OrganizationVisibility {
    return when (this) {
        OrganizationVisibilityDto.PUBLIC -> OrganizationVisibility.PUBLIC
        OrganizationVisibilityDto.PRIVATE -> OrganizationVisibility.PRIVATE
    }
}

fun OrganizationVisibility.toDto(): OrganizationVisibilityDto {
    return when (this) {
        OrganizationVisibility.PUBLIC -> OrganizationVisibilityDto.PUBLIC
        OrganizationVisibility.PRIVATE -> OrganizationVisibilityDto.PRIVATE
    }
}
