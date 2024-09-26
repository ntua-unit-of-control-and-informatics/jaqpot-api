package org.jaqpot.api.service.authentication

import jakarta.ws.rs.BadRequestException
import org.jaqpot.api.ApiKeysApiDelegate
import org.jaqpot.api.entity.ApiKey
import org.jaqpot.api.model.ApiKeyDto
import org.jaqpot.api.model.CreateApiKey201ResponseDto
import org.jaqpot.api.repository.ApiKeyRepository
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.OffsetDateTime
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
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)
        val randomAlphanumeric = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
        return "jq_$randomAlphanumeric"
    }

    fun validateApiKey(apiKey: String?): Optional<ApiKey> {
        return apiKeyRepository.findAllByExpiresAtIsAfter(OffsetDateTime.now())
            .find { key -> bCryptPasswordEncoder.matches(apiKey, key.key) }
            .let { Optional.ofNullable(it) }
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
