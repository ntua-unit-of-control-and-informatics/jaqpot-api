package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.model.OrganizationDto
import org.jaqpot.api.model.OrganizationUserDto
import org.jaqpot.api.model.UserDto

fun Organization.toDto(
    creator: UserDto? = null,
    userCanEdit: Boolean = false,
    isCreator: Boolean = false,
    organizationMembers: List<OrganizationUserDto>
): OrganizationDto {
    return OrganizationDto(
        name = this.name,
        visibility = this.visibility.toDto(),
        contactEmail = this.contactEmail,
        id = this.id,
        creator = creator,
        description = this.description,
        organizationMembers = organizationMembers,
        contactPhone = this.contactPhone,
        website = this.website,
        address = this.address,
        canEdit = userCanEdit,
        isCreator = isCreator,
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
        organizationMembers = mutableListOf(),
        organizationInvitations = mutableListOf(),
        contactEmail = this.contactEmail,
        visibility = this.visibility.toEntity(),
        contactPhone = this.contactPhone,
        website = this.website,
        address = this.address
    )
}

