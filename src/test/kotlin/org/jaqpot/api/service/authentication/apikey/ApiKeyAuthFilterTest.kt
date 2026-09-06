package org.jaqpot.api.service.authentication.apikey

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.DispatcherType
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jaqpot.api.service.authentication.keycloak.KeycloakJwtConverter
import org.jaqpot.api.service.authentication.keycloak.KeycloakTokenExchanger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException

class ApiKeyAuthFilterTest {

    private lateinit var apiKeyService: ApiKeyService
    private lateinit var tokenExchanger: KeycloakTokenExchanger
    private lateinit var jwtDecoder: JwtDecoder
    private lateinit var jwtConverter: KeycloakJwtConverter
    private lateinit var filter: ApiKeyAuthFilter

    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var chain: FilterChain

    @BeforeEach
    fun setUp() {
        apiKeyService = mockk()
        tokenExchanger = mockk()
        jwtDecoder = mockk()
        jwtConverter = mockk()
        filter = ApiKeyAuthFilter(apiKeyService, tokenExchanger, jwtDecoder, jwtConverter)

        request = mockk(relaxed = true)
        response = mockk(relaxed = true)
        chain = mockk(relaxed = true)

        // NB: the request mock must NOT be fully relaxed: OncePerRequestFilter skips
        // doFilterInternal when getAttribute() returns non-null, and a relaxed mockk returns
        // dummy objects (not null) for it. Stub the dispatch-related methods explicitly.
        every { request.getAttribute(any()) } returns null
        every { request.dispatcherType } returns DispatcherType.REQUEST
        every { request.remoteAddr } returns "1.2.3.4"
        every { request.getHeader(ApiKeyAuthFilter.API_KEY_HEADER) } returns "jq_k"
        every { request.getHeader(ApiKeyAuthFilter.API_SECRET_HEADER) } returns "secret"
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `expired api key results in 401, not 500`() {
        every { apiKeyService.validateApiKey(any(), any(), any()) } throws
            ExpiredApiKeyException("API key has expired")

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API key has expired") }
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    @Test
    fun `invalid api key results in 401`() {
        every { apiKeyService.validateApiKey(any(), any(), any()) } throws
            InvalidApiKeyException("Invalid API key")

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key") }
        verify(exactly = 0) { chain.doFilter(any(), any()) }
    }

    @Test
    fun `stale cached token is evicted and exchanged once more`() {
        val cached = ApiKeyAuthCache.CachedApiKey(
            keyId = null,
            userId = "user-1",
            expiresAt = java.time.OffsetDateTime.now().plusMonths(1)
        )
        val jwt = mockk<Jwt>()
        val authentication = mockk<org.springframework.security.authentication.AbstractAuthenticationToken>()
        every { apiKeyService.validateApiKey(any(), any(), any()) } returns cached
        every { tokenExchanger.getOrExchangeToken("user-1") } returns "stale" andThen "fresh"
        every { tokenExchanger.evict("user-1") } just Runs
        every { jwtDecoder.decode("stale") } throws JwtException("expired")
        every { jwtDecoder.decode("fresh") } returns jwt
        every { jwtConverter.convert(jwt) } returns authentication

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { tokenExchanger.evict("user-1") }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `valid api key authenticates and continues chain`() {
        val cached = ApiKeyAuthCache.CachedApiKey(
            keyId = null,
            userId = "user-1",
            expiresAt = java.time.OffsetDateTime.now().plusMonths(1)
        )
        val jwt = mockk<Jwt>()
        val authentication = mockk<org.springframework.security.authentication.AbstractAuthenticationToken>()
        every { apiKeyService.validateApiKey(any(), any(), any()) } returns cached
        every { tokenExchanger.getOrExchangeToken("user-1") } returns "token"
        every { jwtDecoder.decode("token") } returns jwt
        every { jwtConverter.convert(jwt) } returns authentication

        filter.doFilter(request, response, chain)

        verify(exactly = 1) { chain.doFilter(request, response) }
    }
}
