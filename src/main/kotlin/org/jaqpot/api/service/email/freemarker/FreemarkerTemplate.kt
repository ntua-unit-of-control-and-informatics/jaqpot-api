package org.jaqpot.api.service.email.freemarker

enum class FreemarkerTemplate(val path: String) {
    ORGANIZATION_INVITATION("organization-invitation.ftl"),
    LEAD_REQUEST("lead-request.ftl");
}
