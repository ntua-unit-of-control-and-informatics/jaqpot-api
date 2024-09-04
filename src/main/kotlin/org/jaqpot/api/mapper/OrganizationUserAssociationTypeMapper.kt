package org.jaqpot.api.mapper

import org.jaqpot.api.entity.UserOrganizationAssociationType
import org.jaqpot.api.model.OrganizationUserAssociationTypeDto

fun OrganizationUserAssociationTypeDto.toEntity(): UserOrganizationAssociationType {
    return when (this) {
        OrganizationUserAssociationTypeDto.ADMIN -> UserOrganizationAssociationType.ADMIN
        OrganizationUserAssociationTypeDto.MEMBER -> UserOrganizationAssociationType.MEMBER
    }
}

fun UserOrganizationAssociationType.toDto(): OrganizationUserAssociationTypeDto {
    return when (this) {
        UserOrganizationAssociationType.ADMIN -> OrganizationUserAssociationTypeDto.ADMIN
        UserOrganizationAssociationType.MEMBER -> OrganizationUserAssociationTypeDto.MEMBER
    }
}
