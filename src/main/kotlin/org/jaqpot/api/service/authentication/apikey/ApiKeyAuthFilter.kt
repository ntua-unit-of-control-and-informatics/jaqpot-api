package org.jaqpot.api.service.authentication.apikey

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jaqpot.api.service.authentication.keycloak.KeycloakJwtConverter
import org.jaqpot.api.service.authentication.keycloak.KeycloakTokenExchanger
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*


@Component
class ApiKeyAuthFilter(
    private val apiKeyService: ApiKeyService,
    private val keycloakTokenExchanger: KeycloakTokenExchanger,
    private val jwtDecoder: JwtDecoder,
    private val keycloakJwtConverter: KeycloakJwtConverter
) : OncePerRequestFilter() {
    companion object {
        const val API_KEY_HEADER = "X-Api-Key"
        const val API_SECRET_HEADER = "X-Api-Secret"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKeyHeader = extractApiKey(request)
        val apiKeySecret = extractApiSecret(request)

        if (apiKeyHeader.isPresent && apiKeySecret.isPresent) {
            val clientKey = apiKeyHeader.get()
            val clientSecret = apiKeyHeader.get()
            val apiKey = apiKeyService.validateApiKey(clientKey, clientSecret)
            val token: String = keycloakTokenExchanger.exchangeToken(apiKey.userId)
            val jwt: Jwt = jwtDecoder.decode(token)
            val authentication = keycloakJwtConverter.convert(jwt)

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun extractApiKey(request: HttpServletRequest): Optional<String> {
        return Optional.ofNullable(request.getHeader(API_KEY_HEADER))
    }

    private fun extractApiSecret(request: HttpServletRequest): Optional<String> {
        return Optional.ofNullable(request.getHeader(API_SECRET_HEADER))
    }
}
