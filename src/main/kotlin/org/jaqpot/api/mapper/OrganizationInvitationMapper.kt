package org.jaqpot.api.mapper

import org.jaqpot.api.entity.OrganizationInvitation
import org.jaqpot.api.model.OrganizationInvitationResponseDto

fun OrganizationInvitation.toDto(): OrganizationInvitationResponseDto {
    return OrganizationInvitationResponseDto(
        this.userEmail,
        this.status.toDto(),
        this.expirationDate,
        this.id
    )
}

