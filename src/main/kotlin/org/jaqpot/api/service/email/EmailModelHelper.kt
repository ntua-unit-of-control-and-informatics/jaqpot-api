package org.jaqpot.api.service.email

fun generateOrganizationInvitationEmail(actionUrl: String, recipientName: String = ""): Map<String, Any> {
    return mapOf(
        "title" to "Invitation to Join a Jaqpot Organization",
        "recipientName" to recipientName,
        "bodyContent" to "You have been invited to join the Jaqpot organization, Jaqpot-org.",
        "actionUrl" to actionUrl,
        "actionText" to "Join the Organization"
    )
}
