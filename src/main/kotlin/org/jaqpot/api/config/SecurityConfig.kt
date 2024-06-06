package org.jaqpot.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                // allow all swagger urls
                arrayOf("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").map { it ->
                    authorize(it, permitAll)
                }

                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt { }
            }
            csrf { disable() }
        }
        return http.build()
    }

}
