package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.model.OrganizationDto
import org.jaqpot.api.model.UserDto

fun Organization.toDto(userDto: UserDto): OrganizationDto {
    return OrganizationDto(
        this.name,
        this.userIds.toList(),
        emptyList(),
        this.id,
        this.creatorId,
        this.description,
        this.contactEmail,
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
        this.userIds.toSet(),
        mutableSetOf(),
        this.contactEmail,
        this.contactPhone,
        this.website,
        this.address
    )
}

