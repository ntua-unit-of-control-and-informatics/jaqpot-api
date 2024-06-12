package org.jaqpot.api.service.email.smtp

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.mail")
data class SMTPConfig(
    val from: String,
)
