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
                "actionUrl" to actionUrl,
                "actionText" to "Join the Organization"
            )
        }

        fun generateLeadRequestEmailModel(
            recipientName: String = ""
        ): Map<String, Any> {
            return mapOf(
                "title" to "Early access request received",
                "recipientName" to recipientName,
                "actionText" to "Join the Organization"
            )
        }
    }
}

