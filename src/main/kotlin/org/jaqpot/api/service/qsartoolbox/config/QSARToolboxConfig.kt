package org.jaqpot.api.service.qsartoolbox.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jaqpot.qsartoolbox")
data class QsartoolboxConfig(
    val url: String
)
