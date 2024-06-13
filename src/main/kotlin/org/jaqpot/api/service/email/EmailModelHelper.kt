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
                "bodyContent" to "You have been invited to join the Jaqpot organization: <b>$orgName</b>. Please note that this invitation will expire in one week.\n" +
                        "<br><br>" +
                        "Upon accepting the invitation, you will have the ability to view and execute all the models shared with that organization.\n" +
                        "<br><br>",
                "actionUrl" to actionUrl,
                "actionText" to "Join the Organization"
            )
        }
    }
}

