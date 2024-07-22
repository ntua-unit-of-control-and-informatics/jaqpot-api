package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.model.OrganizationDto

fun Organization.toDto(userCanEdit: Boolean = false): OrganizationDto {
    return OrganizationDto(
        name = this.name,
        visibility = this.visibility.toDto(),
        contactEmail = this.contactEmail,
        id = this.id,
        creatorId = this.creatorId,
        description = this.description,
        userIds = this.userIds.toList(),
        contactPhone = this.contactPhone,
        website = this.website,
        address = this.address,
        canEdit = userCanEdit,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun OrganizationDto.toEntity(adminUserId: String): Organization {
    return Organization(
        id = this.id,
        name = this.name,
        creatorId = adminUserId,
        description = this.description,
        userIds = this.userIds?.toMutableSet() ?: mutableSetOf(),
        models = mutableSetOf(),
        organizationInvitations = mutableListOf(),
        contactEmail = this.contactEmail,
        visibility = this.visibility.toEntity(),
        contactPhone = this.contactPhone,
        website = this.website,
        address = this.address
    )
}

