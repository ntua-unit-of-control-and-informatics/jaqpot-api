package org.jaqpot.api.service.authentication.apikey

import jakarta.ws.rs.BadRequestException
import org.apache.commons.lang3.RandomStringUtils
import org.jaqpot.api.ApiKeysApiDelegate
import org.jaqpot.api.entity.ApiKey
import org.jaqpot.api.model.ApiKeyDto
import org.jaqpot.api.model.CreateApiKey201ResponseDto
import org.jaqpot.api.repository.ApiKeyRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.password.PasswordEncoder
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset


@Service
class ApiKeyService(
    private val apiKeyRepository: ApiKeyRepository,
    private val authenticationFacade: AuthenticationFacade,
    private val passwordEncoder: PasswordEncoder
) : ApiKeysApiDelegate {


    fun generateClientKey(): String {
        val randomAlphanumeric = RandomStringUtils.randomAlphanumeric(24)
        return "jq_$randomAlphanumeric"
    }

    fun generateClientSecret(): String {
        val randomAlphanumeric = RandomStringUtils.randomAlphanumeric(50)
        return randomAlphanumeric
    }

    fun validateApiKey(clientKey: String, clientSecret: String, ip: String): ApiKey {
        val todayStart = getStartOfToday()
        return apiKeyRepository.findByClientKey(clientKey)?.let { apiKey ->
            if (!apiKey.enabled) {
                throw BadRequestException("API key is disabled")
            } else if (apiKey.expiresAt.isBefore(todayStart)) {
                throw BadRequestException("API key has expired")
            } else if (passwordEncoder.matches(clientSecret, apiKey.clientSecret)) {
                apiKeyRepository.updateLastUsed(apiKey.id, OffsetDateTime.now(), ip)
                return apiKey
            }
            throw BadRequestException("Invalid API key")
        } ?: throw BadRequestException("Invalid API key")
    }

    /**
     * Using this to properly cache the start of today for the rate limiting.
     */
    private fun getStartOfToday(): OffsetDateTime {
        return OffsetDateTime.now(ZoneOffset.UTC)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
    }

    @WithRateLimitProtectionByUser(limit = 5, intervalInSeconds = 60 * 10)
    override fun createApiKey(apiKeyDto: ApiKeyDto): ResponseEntity<CreateApiKey201ResponseDto> {
        val clientKey = generateClientKey()
        val clientSecret = generateClientSecret()
        val expiresAt = when (apiKeyDto.expirationTime) {
            ApiKeyDto.ExpirationTime.THREE_MONTHS -> {
                OffsetDateTime.now().plusMonths(3)
            }

            ApiKeyDto.ExpirationTime.SIX_MONTHS -> {
                OffsetDateTime.now().plusMonths(6)
            }

            else -> {
                throw BadRequestException("Invalid expiration time")
            }
        }
        val apiKey =
            ApiKey(
                clientKey = clientKey,
                clientSecret = passwordEncoder.encode(clientSecret),
                userId = authenticationFacade.userId,
                note = apiKeyDto.note,
                expiresAt = expiresAt,
                enabled = true
            )

        apiKeyRepository.save(apiKey)

        // Return the clear text client secret to the user. This is the only time it will be available in clear text.
        return ResponseEntity.ok().body(CreateApiKey201ResponseDto(clientKey = clientKey, clientSecret = clientSecret))
    }
}
