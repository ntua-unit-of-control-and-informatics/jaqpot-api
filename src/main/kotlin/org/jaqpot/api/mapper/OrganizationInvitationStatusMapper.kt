package org.jaqpot.api.mapper

import org.jaqpot.api.entity.OrganizationInvitationStatus
import org.jaqpot.api.model.OrganizationInvitationResponseDto

fun OrganizationInvitationResponseDto.Status.toEntity(): OrganizationInvitationStatus {
    return when (this) {
        OrganizationInvitationResponseDto.Status.PENDING -> OrganizationInvitationStatus.PENDING
        OrganizationInvitationResponseDto.Status.REJECTED -> OrganizationInvitationStatus.REJECTED
        OrganizationInvitationResponseDto.Status.ACCEPTED -> OrganizationInvitationStatus.ACCEPTED
    }
}

fun OrganizationInvitationStatus.toDto(): OrganizationInvitationResponseDto.Status {
    return when (this) {
        OrganizationInvitationStatus.PENDING -> OrganizationInvitationResponseDto.Status.PENDING
        OrganizationInvitationStatus.REJECTED -> OrganizationInvitationResponseDto.Status.REJECTED
        OrganizationInvitationStatus.ACCEPTED -> OrganizationInvitationResponseDto.Status.ACCEPTED
    }
}
