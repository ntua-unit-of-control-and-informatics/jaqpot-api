package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.entity.OrganizationUserAssociation
import org.jaqpot.api.entity.OrganizationUserAssociationType
import org.jaqpot.api.model.OrganizationUserDto

fun OrganizationUserDto.toUserOrganizationAssociationEntity(org: Organization): OrganizationUserAssociation {
    return OrganizationUserAssociation(
        id = null,
        userId = this.userId,
        organization = org,
        OrganizationUserAssociationType.MEMBER
    )
}

fun OrganizationUserAssociation.toOrganizationUserDto(
    username: String?,
    userEmail: String?,
    avatarUrl: String?
): OrganizationUserDto {
    return OrganizationUserDto(
        userId = this.userId,
        username = username,
        email = userEmail,
        associationType = this.associationType.toDto(),
        avatarUrl = avatarUrl
    )
}
