package org.jaqpot.api.config

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class TomcatAsyncConfig {
    @Bean
    fun asyncTimeoutCustomize(): TomcatConnectorCustomizer {
        return TomcatConnectorCustomizer { connector ->
            connector.asyncTimeout = 180_000 // 3 minutes
        }
    }
}
