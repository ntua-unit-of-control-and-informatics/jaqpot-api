package org.jaqpot.api.service.email.smtp

import jakarta.mail.internet.MimeMessage
import org.jaqpot.api.service.email.EmailService
import org.jaqpot.api.service.email.freemarker.FreemarkerTemplate
import org.jaqpot.api.service.email.freemarker.FreemarkerTemplateEngine
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

class SMTPEmailService(
    private val smtpConfig: SMTPConfig,
    private val templateEngine: FreemarkerTemplateEngine,
    private val javaMailSender: JavaMailSender
) : EmailService<FreemarkerTemplate> {
    override fun sendHTMLEmail(to: String, subject: String, template: FreemarkerTemplate, model: Map<String, Any>?) {
        val message: MimeMessage = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setFrom(smtpConfig.from)
        helper.setTo(to)
        helper.setSubject(subject)

        val htmlBody = templateEngine.convertToHTML(template, model)

        helper.setText(htmlBody, true)
        javaMailSender.send(message)
    }
}
