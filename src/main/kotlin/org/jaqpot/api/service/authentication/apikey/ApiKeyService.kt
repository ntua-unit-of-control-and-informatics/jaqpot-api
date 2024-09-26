package org.jaqpot.api.service.authentication.apikey

import jakarta.ws.rs.BadRequestException
import org.apache.commons.lang3.RandomStringUtils
import org.jaqpot.api.ApiKeysApiDelegate
import org.jaqpot.api.entity.ApiKey
import org.jaqpot.api.model.ApiKeyDto
import org.jaqpot.api.model.CreateApiKey201ResponseDto
import org.jaqpot.api.repository.ApiKeyRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*


@Service
class ApiKeyService(
    private val apiKeyRepository: ApiKeyRepository,
    private val authenticationFacade: AuthenticationFacade
) : ApiKeysApiDelegate {

    companion object {
        val bCryptPasswordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()
    }

    fun generateApiKey(): String {
        val randomAlphanumeric = RandomStringUtils.randomAlphanumeric(32)
        return "jq_$randomAlphanumeric"
    }

    fun validateApiKey(apiKey: String?): Optional<ApiKey> {
        val todayStart = getStartOfToday()
        return apiKeyRepository.findAllByExpiresAtIsAfter(todayStart)
            .find { key -> bCryptPasswordEncoder.matches(apiKey, key.key) }
            .let { Optional.ofNullable(it) }
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
        val apiKey = generateApiKey()
        val hashedKey: String = bCryptPasswordEncoder.encode(apiKey)
        if (apiKeyDto.expiresAt != null) {
            if (apiKeyDto.expiresAt!!.isBefore(OffsetDateTime.now())) {
                throw BadRequestException("The expiration date must be in the future.")
            } else if (apiKeyDto.expiresAt!!.isAfter(OffsetDateTime.now())) {
                throw BadRequestException("The expiration date must be within 1 year from now.")
            }
        }
        val expiresAt = apiKeyDto.expiresAt ?: OffsetDateTime.now().plusMonths(3)

        val newApiKey = ApiKey(key = hashedKey, authenticationFacade.userId, expiresAt = expiresAt)

        apiKeyRepository.save(newApiKey)

        // Return the clear text API key to the user. This is the only time it will be available in clear text.
        return ResponseEntity.ok().body(CreateApiKey201ResponseDto(key = apiKey))
    }
}
