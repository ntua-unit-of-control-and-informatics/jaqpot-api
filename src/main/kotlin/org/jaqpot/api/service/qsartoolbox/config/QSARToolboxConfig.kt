package org.jaqpot.api.service.qsartoolbox.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jaqpot.qsartoolbox")
data class QsartoolboxConfig(
    val url: String,
    /**
     * Stricter per-user hourly allowance for QSAR Toolbox predictions.
     * The toolbox runs on a single small Windows EC2 host and restarts under
     * flood, so this is intentionally lower than the generic prediction limit.
     */
    val rateLimitPerHour: Long = 10,
    /**
     * Max concurrent outbound HTTP calls to the toolbox across the whole instance.
     * Excess calls are rejected with 429 instead of piling onto the toolbox.
     */
    val maxConcurrentRequests: Int = 2
)
