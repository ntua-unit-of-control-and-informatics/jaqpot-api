package org.jaqpot.api.service.authentication.apikey

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jaqpot.api.entity.ApiKey
import org.jaqpot.api.model.UpdateApiKeyRequestDto
import org.jaqpot.api.repository.ApiKeyRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.password.PasswordEncoder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class ApiKeyServiceTest {

    private lateinit var apiKeyRepository: ApiKeyRepository
    private lateinit var authenticationFacade: AuthenticationFacade
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var apiKeyUsageService: ApiKeyUsageService
    private lateinit var apiKeyService: ApiKeyService

    private val keyId = UUID.randomUUID()
    private val clientKey = "jq_testkey123"
    private val clientSecret = "secret"
    private val storedHash = "bcrypt-hash"
    private val userId = "user-1"

    private fun apiKey(
        enabled: Boolean = true,
        expiresAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1)
    ) = ApiKey(
        id = keyId,
        clientKey = clientKey,
        clientSecret = storedHash,
        userId = userId,
        expiresAt = expiresAt,
        enabled = enabled
    )

    @BeforeEach
    fun setUp() {
        apiKeyRepository = mockk()
        authenticationFacade = mockk()
        passwordEncoder = mockk()
        apiKeyUsageService = mockk(relaxed = true)
        apiKeyService = ApiKeyService(
            apiKeyRepository,
            authenticationFacade,
            passwordEncoder,
            ApiKeyAuthCache(),
            apiKeyUsageService
        )
    }

    @Test
    fun `valid key returns user and second call is served from cache`() {
        every { apiKeyRepository.findByClientKey(clientKey) } returns apiKey()
        every { passwordEncoder.matches(clientSecret, storedHash) } returns true

        val first = apiKeyService.validateApiKey(clientKey, clientSecret, "1.2.3.4")
        val second = apiKeyService.validateApiKey(clientKey, clientSecret, "1.2.3.4")

        assertEquals(userId, first.userId)
        assertEquals(userId, second.userId)
        verify(exactly = 1) { apiKeyRepository.findByClientKey(clientKey) }
        verify(exactly = 2) { apiKeyUsageService.recordUsage(keyId, "1.2.3.4") }
    }

    @Test
    fun `unknown key throws InvalidApiKeyException and is not cached`() {
        every { apiKeyRepository.findByClientKey(clientKey) } returns null

        assertThrows<InvalidApiKeyException> { apiKeyService.validateApiKey(clientKey, clientSecret, "ip") }
        assertThrows<InvalidApiKeyException> { apiKeyService.validateApiKey(clientKey, clientSecret, "ip") }
        verify(exactly = 2) { apiKeyRepository.findByClientKey(clientKey) }
    }

    @Test
    fun `wrong secret throws InvalidApiKeyException and is not cached`() {
        every { apiKeyRepository.findByClientKey(clientKey) } returns apiKey()
        every { passwordEncoder.matches(clientSecret, storedHash) } returns false

        assertThrows<InvalidApiKeyException> { apiKeyService.validateApiKey(clientKey, clientSecret, "ip") }
        assertThrows<InvalidApiKeyException> { apiKeyService.validateApiKey(clientKey, clientSecret, "ip") }
        verify(exactly = 2) { apiKeyRepository.findByClientKey(clientKey) }
    }

    @Test
    fun `disabled key throws InvalidApiKeyException`() {
        every { apiKeyRepository.findByClientKey(clientKey) } returns apiKey(enabled = false)

        assertThrows<InvalidApiKeyException> { apiKeyService.validateApiKey(clientKey, clientSecret, "ip") }
    }

    @Test
    fun `expired key throws ExpiredApiKeyException which is an InvalidApiKeyException (maps to 401)`() {
        every { apiKeyRepository.findByClientKey(clientKey) } returns apiKey(
            expiresAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)
        )

        // Compile-time + runtime guard for the 500-on-expired-key bug: the auth filter catches
        // InvalidApiKeyException, so ExpiredApiKeyException must be assignable to it. If someone
        // breaks the hierarchy, this stops compiling.
        val ex: InvalidApiKeyException = assertThrows<ExpiredApiKeyException> {
            apiKeyService.validateApiKey(clientKey, clientSecret, "ip")
        }
        assertEquals("API key has expired", ex.message)
    }

    @Test
    fun `expired key failure is not cached`() {
        every { apiKeyRepository.findByClientKey(clientKey) } returns apiKey(
            expiresAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)
        )

        assertThrows<ExpiredApiKeyException> { apiKeyService.validateApiKey(clientKey, clientSecret, "ip") }
        assertThrows<ExpiredApiKeyException> { apiKeyService.validateApiKey(clientKey, clientSecret, "ip") }
        verify(exactly = 2) { apiKeyRepository.findByClientKey(clientKey) }
    }

    @Test
    fun `updateApiKey evicts cached validation`() {
        // Fresh instance per call: updateApiKey mutates the entity it loads, and sharing one
        // instance between stubbings would leak that mutation into later validations.
        every { apiKeyRepository.findByClientKey(clientKey) } answers { apiKey() }
        every { passwordEncoder.matches(clientSecret, storedHash) } returns true
        every { apiKeyRepository.save(any()) } returnsArgument 0

        apiKeyService.validateApiKey(clientKey, clientSecret, "ip")
        apiKeyService.updateApiKey(clientKey, UpdateApiKeyRequestDto(note = "n", enabled = false))
        apiKeyService.validateApiKey(clientKey, clientSecret, "ip")

        // Once for the first validation, once inside updateApiKey, once after eviction.
        verify(exactly = 3) { apiKeyRepository.findByClientKey(clientKey) }
    }

    @Test
    fun `deleteApiKey evicts cached validation`() {
        every { apiKeyRepository.findByClientKey(clientKey) } returns apiKey()
        every { passwordEncoder.matches(clientSecret, storedHash) } returns true
        every { apiKeyRepository.delete(any()) } returns Unit

        apiKeyService.validateApiKey(clientKey, clientSecret, "ip")
        apiKeyService.deleteApiKey(clientKey)
        // Key is gone from the database now; without eviction the stale cache entry would win.
        every { apiKeyRepository.findByClientKey(clientKey) } returns null
        assertThrows<InvalidApiKeyException> { apiKeyService.validateApiKey(clientKey, clientSecret, "ip") }

        verify(exactly = 1) { apiKeyRepository.delete(any()) }
    }
}
