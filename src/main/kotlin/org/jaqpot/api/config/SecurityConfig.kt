package org.jaqpot.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { a ->
            a
                // allow swagger for everyone
                .requestMatchers(
                    "/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
                )
                .permitAll()
                
                .requestMatchers(
                    "/models/**"
                )
                .permitAll()

                .anyRequest()
                .authenticated()

        }
            .oauth2ResourceServer { oauth2: OAuth2ResourceServerConfigurer<HttpSecurity?> -> oauth2.jwt(Customizer.withDefaults()) }
        return http.build()
    }
}
