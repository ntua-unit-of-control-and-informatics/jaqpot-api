package org.jaqpot.api.mapper

import org.jaqpot.api.entity.OrganizationVisibility
import org.jaqpot.api.model.OrganizationDto

fun OrganizationDto.Visibility.toEntity(): OrganizationVisibility {
    return when (this) {
        OrganizationDto.Visibility.PUBLIC -> OrganizationVisibility.PUBLIC
        org.jaqpot.api.model.OrganizationDto.Visibility.PRIVATE -> OrganizationVisibility.PRIVATE
    }
}

fun OrganizationVisibility.toDto(): OrganizationDto.Visibility {
    return when (this) {
        OrganizationVisibility.PUBLIC -> OrganizationDto.Visibility.PUBLIC
        OrganizationVisibility.PRIVATE -> OrganizationDto.Visibility.PRIVATE
    }
}
