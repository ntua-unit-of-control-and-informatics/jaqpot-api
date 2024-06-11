package org.jaqpot.api.service.email

interface EmailService<T> {
    fun sendHTMLEmail(to: String, subject: String, template: T, model: Map<String, Any>?)
}
