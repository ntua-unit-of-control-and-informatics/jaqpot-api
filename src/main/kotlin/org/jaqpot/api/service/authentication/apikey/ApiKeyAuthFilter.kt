package org.jaqpot.api.service.authentication.apikey

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jaqpot.api.service.authentication.keycloak.KeycloakJwtConverter
import org.jaqpot.api.service.authentication.keycloak.KeycloakTokenExchanger
import org.jaqpot.api.service.util.IPUtil
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
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
            val clientSecret = apiKeySecret.get()
            val ip = IPUtil.getIPFromHeader(request)
            val apiKey = try {
                apiKeyService.validateApiKey(clientKey, clientSecret, ip)
            } catch (e: InvalidApiKeyException) {
                // Covers unknown/disabled/wrong-secret keys as well as expired ones
                // (ExpiredApiKeyException extends InvalidApiKeyException).
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
                return
            }

            try {
                SecurityContextHolder.getContext().authentication = authenticate(apiKey.userId)
            } catch (e: Exception) {
                // Keycloak/JWT failures are server-side problems: 500, but with a clean status
                // instead of an unhandled filter exception.
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failed")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Builds the Spring authentication from a cached-or-fresh Keycloak impersonation token.
     * If a cached token goes stale between fetch and decode, it is dropped and exchanged once
     * more. Exchange failures (Keycloak down, user gone) propagate immediately without retry.
     */
    private fun authenticate(userId: String): Authentication {
        val token = keycloakTokenExchanger.getOrExchangeToken(userId)
        try {
            return convert(token)
        } catch (e: JwtException) {
            keycloakTokenExchanger.evict(userId)
            return convert(keycloakTokenExchanger.getOrExchangeToken(userId))
        }
    }

    private fun convert(token: String): Authentication {
        val jwt: Jwt = jwtDecoder.decode(token)
        return keycloakJwtConverter.convert(jwt)
    }

    private fun extractApiKey(request: HttpServletRequest): Optional<String> {
        return Optional.ofNullable(request.getHeader(API_KEY_HEADER))
    }

    private fun extractApiSecret(request: HttpServletRequest): Optional<String> {
        return Optional.ofNullable(request.getHeader(API_SECRET_HEADER))
    }
}
