package org.jaqpot.api.config

import jakarta.servlet.DispatcherType
import org.jaqpot.api.service.authentication.keycloak.KeycloakJwtConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val keycloakJwtConverter: KeycloakJwtConverter
//    private val modelAuthorizationManager: ModelAuthorizationManager
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                // TODO see if these 2 lines are needed @see https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html#_all_dispatches_are_authorized
                authorize(DispatcherTypeRequestMatcher(DispatcherType.FORWARD), permitAll)
                authorize(DispatcherTypeRequestMatcher(DispatcherType.ERROR), permitAll)
                // allow all swagger urls
                arrayOf("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").map { it ->
                    authorize(it, permitAll)
                }
                authorize("/actuator/**", permitAll)
                authorize("/v1/models/{modelId:[\\d+]}", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = keycloakJwtConverter
                }
            }
            csrf { disable() }
        }
        return http.build()
    }

}
