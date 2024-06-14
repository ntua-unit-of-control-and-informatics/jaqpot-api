package org.jaqpot.api.service.email

import org.jaqpot.api.service.email.freemarker.FreemarkerTemplate

interface EmailService {
    fun sendHTMLEmail(to: String, subject: String, template: FreemarkerTemplate, model: Map<String, Any>?)
}
