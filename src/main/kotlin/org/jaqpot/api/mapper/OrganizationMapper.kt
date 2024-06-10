package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.model.OrganizationDto

fun Organization.toDto(): OrganizationDto {
    return OrganizationDto(
        this.name,
        this.contactEmail,
        this.visibility.toDto(),
        this.id,
        this.creatorId,
        this.description,
        this.userIds.toList(),
        emptyList(),
        this.contactPhone,
        this.website,
        this.address,
        this.createdAt,
        this.updatedAt
    )
}

fun OrganizationDto.toEntity(adminUserId: String): Organization {
    return Organization(
        this.id,
        this.name,
        adminUserId,
        this.description,
        this.userIds?.toSet() ?: emptySet<String>(),
        mutableSetOf(),
        this.contactEmail,
        this.visibility.toEntity(),
        this.contactPhone,
        this.website,
        this.address
    )
}

