package org.jaqpot.api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class JaqpotConfig(
    @Value("\${jaqpot.frontend.url}")
    val frontendUrl: String
)
