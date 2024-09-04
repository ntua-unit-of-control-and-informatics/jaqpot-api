package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.model.OrganizationDto
import org.jaqpot.api.model.OrganizationUserDto

fun Organization.toDto(
    userCanEdit: Boolean = false,
    isCreator: Boolean = false,
    organizationMembers: List<OrganizationUserDto>
): OrganizationDto {
    return OrganizationDto(
        name = this.name,
        visibility = this.visibility.toDto(),
        contactEmail = this.contactEmail,
        id = this.id,
        creatorId = this.creatorId,
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
    val o = Organization(
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

    this.organizationMembers?.let { organizationMemberDtos ->
        o.organizationMembers.addAll(organizationMemberDtos.map {
            it.toUserOrganizationAssociationEntity(
                o
            )
        })
    }

    return o
}

