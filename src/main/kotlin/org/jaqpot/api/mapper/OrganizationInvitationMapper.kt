package org.jaqpot.api.mapper

import org.jaqpot.api.entity.OrganizationInvitation
import org.jaqpot.api.model.OrganizationInvitationDto

fun OrganizationInvitation.toDto(): OrganizationInvitationDto {
    return OrganizationInvitationDto(
        this.userEmail,
        this.status.toDto(),
        this.expirationDate,
        this.id
    )
}

