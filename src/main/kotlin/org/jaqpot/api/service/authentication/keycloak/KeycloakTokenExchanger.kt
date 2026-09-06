package org.jaqpot.api.service.authentication.keycloak

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import com.nimbusds.jwt.JWTParser
import org.jaqpot.api.error.JaqpotRuntimeException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.concurrent.TimeUnit


@Component
class KeycloakTokenExchanger(
    private val keycloakConfig: KeycloakConfig,
    private val restTemplate: RestTemplate
) {
    companion object {
        private const val MAX_SIZE = 10_000L

        /**
         * Tokens are dropped from the cache this far before their real expiry, so a cached token
         * is never handed out in a state where the subsequent signature/expiry check would reject
         * it under normal clock conditions.
         */
        private const val EXPIRY_SKEW_SECONDS = 30L

        /** Only cache tokens that stay valid for at least this long; shorter-lived ones are returned uncached. */
        private const val MIN_CACHEABLE_LIFETIME_SECONDS = 60L
    }

    private data class CachedToken(val token: String, val expiresAt: Instant)

    /**
     * Per-user impersonation tokens. Entries expire at the token's own `exp` claim minus a skew,
     * never later. Every token served from here is still passed through the signature/expiry
     * check (`JwtDecoder`) by the caller before use, so a stale entry can only cause a rejected
     * request, never an accepted forged one.
     */
    private val tokenCache: Cache<String, CachedToken> = Caffeine.newBuilder()
        .maximumSize(MAX_SIZE)
        .expireAfter(object : Expiry<String, CachedToken> {
            override fun expireAfterCreate(key: String, value: CachedToken, currentTime: Long): Long {
                val remaining = java.time.Duration.between(Instant.now(), value.expiresAt)
                    .minusSeconds(EXPIRY_SKEW_SECONDS)
                return remaining.toNanos().coerceAtLeast(0)
            }

            override fun expireAfterUpdate(
                key: String,
                value: CachedToken,
                currentTime: Long,
                currentDuration: Long
            ): Long = currentDuration

            override fun expireAfterRead(
                key: String,
                value: CachedToken,
                currentTime: Long,
                currentDuration: Long
            ): Long = currentDuration
        })
        .build()

    /**
     * Returns a cached impersonation token for the user, exchanging a fresh one with Keycloak on
     * miss. At most one in-flight exchange per user: [Cache.get] runs the loader atomically.
     */
    fun getOrExchangeToken(userId: String): String {
        val cached = tokenCache.getIfPresent(userId)
        if (cached != null && cached.expiresAt.isAfter(Instant.now().plusSeconds(EXPIRY_SKEW_SECONDS))) {
            return cached.token
        }
        val fresh = exchangeToken(userId)
        val expiresAt = tokenExpiry(fresh)
        if (expiresAt != null &&
            java.time.Duration.between(Instant.now(), expiresAt).seconds > MIN_CACHEABLE_LIFETIME_SECONDS
        ) {
            tokenCache.put(userId, CachedToken(fresh, expiresAt))
        }
        return fresh
    }

    fun evict(userId: String) {
        tokenCache.invalidate(userId)
    }

    /** Parses the `exp` claim without verifying the signature (verification happens downstream). */
    private fun tokenExpiry(token: String): Instant? {
        return try {
            JWTParser.parse(token).jwtClaimsSet.getDateClaim("exp")?.toInstant()
        } catch (e: Exception) {
            null
        }
    }
    /**
     * Exchange a user token for an impersonation token
     * @see <a href="https://www.keycloak.org/docs/24.0.3/securing_apps/index.html#direct-naked-impersonation">https://www.keycloak.org/docs/24.0.3/securing_apps/index.html#direct-naked-impersonation</a>
     */
    fun exchangeToken(userId: String): String {
        // Set headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        // Create body with required parameters
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", keycloakConfig.impersonationClientId)
        body.add("client_secret", keycloakConfig.impersonationClientSecret)
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
        body.add("requested_subject", userId) // The user to impersonate

        // Create the request entity
        val requestEntity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(body, headers)

        // Send the request
        val response: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(
            "${keycloakConfig.serverUrl}/realms/${keycloakConfig.realm}/protocol/openid-connect/token",
            requestEntity,
            Map::class.java
        )

        // Extract the access token from the response
        if (response.statusCode.is2xxSuccessful && response.body != null) {
            return response.body!!["access_token"].toString()
        } else {
            throw JaqpotRuntimeException("Failed to exchange token")
        }
    }
}
