package org.jaqpot.api.config

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.observation.ServerRequestObservationContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

@Configuration
class MetricsConfig {

    @Bean
    fun userObservationFilter(): ObservationFilter {
        return ObservationFilter { context ->
            if (context is ServerRequestObservationContext) {
                val authentication = SecurityContextHolder.getContext().authentication
                val userTag = when {
                    authentication is JwtAuthenticationToken -> {
                        authentication.tokenAttributes["preferred_username"] as? String
                            ?: authentication.name
                    }
                    authentication != null && authentication.isAuthenticated -> {
                        authentication.name
                    }
                    else -> "anonymous"
                }
                context.addLowCardinalityKeyValue(KeyValue.of("user", userTag))
            }
            context
        }
    }
}
