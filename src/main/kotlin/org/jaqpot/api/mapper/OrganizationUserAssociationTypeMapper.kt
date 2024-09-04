package org.jaqpot.api.mapper

import org.jaqpot.api.entity.OrganizationUserAssociationType
import org.jaqpot.api.model.OrganizationUserAssociationTypeDto

fun OrganizationUserAssociationTypeDto.toEntity(): OrganizationUserAssociationType {
    return when (this) {
        OrganizationUserAssociationTypeDto.ADMIN -> OrganizationUserAssociationType.ADMIN
        OrganizationUserAssociationTypeDto.MEMBER -> OrganizationUserAssociationType.MEMBER
    }
}

fun OrganizationUserAssociationType.toDto(): OrganizationUserAssociationTypeDto {
    return when (this) {
        OrganizationUserAssociationType.ADMIN -> OrganizationUserAssociationTypeDto.ADMIN
        OrganizationUserAssociationType.MEMBER -> OrganizationUserAssociationTypeDto.MEMBER
    }
}
