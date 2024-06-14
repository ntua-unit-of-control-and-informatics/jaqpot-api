package org.jaqpot.api.mapper

import org.jaqpot.api.entity.OrganizationInvitationStatus
import org.jaqpot.api.model.OrganizationInvitationDto

fun OrganizationInvitationDto.Status.toEntity(): OrganizationInvitationStatus {
    return when (this) {
        OrganizationInvitationDto.Status.PENDING -> OrganizationInvitationStatus.PENDING
        OrganizationInvitationDto.Status.REJECTED -> OrganizationInvitationStatus.REJECTED
        OrganizationInvitationDto.Status.ACCEPTED -> OrganizationInvitationStatus.ACCEPTED
    }
}

fun OrganizationInvitationStatus.toDto(): OrganizationInvitationDto.Status {
    return when (this) {
        OrganizationInvitationStatus.PENDING -> OrganizationInvitationDto.Status.PENDING
        OrganizationInvitationStatus.REJECTED -> OrganizationInvitationDto.Status.REJECTED
        OrganizationInvitationStatus.ACCEPTED -> OrganizationInvitationDto.Status.ACCEPTED
    }
}
