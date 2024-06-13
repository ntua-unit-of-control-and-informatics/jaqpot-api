package org.jaqpot.api.service.email

class EmailModelHelper {
    companion object {
        fun generateOrganizationInvitationEmailModel(
            actionUrl: String,
            orgName: String,
            recipientName: String = ""
        ): Map<String, Any> {
            return mapOf(
                "title" to "Invitation to join a Jaqpot Organization",
                "recipientName" to recipientName,
                "bodyContent" to "You have been invited to join the Jaqpot organization, $orgName. " +
                        "This invitation will expire in 1 week from now",
                "actionUrl" to actionUrl,
                "actionText" to "Join the Organization"
            )
        }
    }
}

