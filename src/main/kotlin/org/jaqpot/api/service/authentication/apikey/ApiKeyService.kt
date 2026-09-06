package org.jaqpot.api.service.authentication.apikey

import org.apache.commons.lang3.RandomStringUtils
import org.jaqpot.api.ApiKeysApiDelegate
import org.jaqpot.api.entity.ApiKey
import org.jaqpot.api.model.*
import org.jaqpot.api.repository.ApiKeyRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.password.PasswordEncoder
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime


@Service
class ApiKeyService(
    private val apiKeyRepository: ApiKeyRepository,
    private val authenticationFacade: AuthenticationFacade,
    private val passwordEncoder: PasswordEncoder,
    private val apiKeyAuthCache: ApiKeyAuthCache,
    private val apiKeyUsageService: ApiKeyUsageService
) : ApiKeysApiDelegate {

    override fun getAllApiKeysForUser(): ResponseEntity<List<GetAllApiKeysForUser200ResponseInnerDto>> {
        val apiKeys = apiKeyRepository.findAllByUserId(authenticationFacade.userId)
        return ResponseEntity.ok(apiKeys.map {
            GetAllApiKeysForUser200ResponseInnerDto(
                clientKey = it.clientKey,
                note = it.note,
                createdAt = it.createdAt,
                expiresAt = it.expiresAt,
                enabled = it.enabled
            )
        })
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

    @WithRateLimitProtectionByUser(limit = 10, intervalInSeconds = 60)
    @PreAuthorize("@getApiKeyAuthorizationLogic.decide(#root, #key)")
    override fun updateApiKey(
        key: String,
        updateApiKeyRequestDto: UpdateApiKeyRequestDto
    ): ResponseEntity<UpdateApiKey200ResponseDto> {
        val existingApiKey = apiKeyRepository.findByClientKey(key) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Api key not found"
        )
        updateApiKeyRequestDto.note?.let { existingApiKey.note = it }
        updateApiKeyRequestDto.enabled?.let { existingApiKey.enabled = it }

        apiKeyRepository.save(existingApiKey)
        apiKeyAuthCache.evictByClientKey(key)

        return ResponseEntity.ok().body(UpdateApiKey200ResponseDto(key, existingApiKey.note, existingApiKey.enabled))
    }

    @WithRateLimitProtectionByUser(limit = 10, intervalInSeconds = 60)
    @PreAuthorize("@getApiKeyAuthorizationLogic.decide(#root, #key)")
    override fun deleteApiKey(key: String): ResponseEntity<Unit> {
        val existingApiKey = apiKeyRepository.findByClientKey(key) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Api key not found"
        )
        apiKeyRepository.delete(existingApiKey)
        apiKeyAuthCache.evictByClientKey(key)
        return ResponseEntity.noContent().build()
    }

    fun generateClientKey(): String {
        val randomAlphanumeric = RandomStringUtils.randomAlphanumeric(24)
        return "jq_$randomAlphanumeric"
    }

    fun generateClientSecret(): String {
        val randomAlphanumeric = RandomStringUtils.randomAlphanumeric(50)
        return randomAlphanumeric
    }

    /**
     * Validates an API key. Successful validations are served from [ApiKeyAuthCache] (which honors
     * the key's own `expiresAt`, see [ApiKeyAuthCache.isExpired]); failures always hit the database
     * so unknown, wrong, disabled or expired keys are evaluated fresh on every attempt.
     *
     * Usage tracking is recorded asynchronously and throttled (see [ApiKeyUsageService]).
     */
    fun validateApiKey(
        clientKey: String,
        clientSecret: String,
        ip: String
    ): ApiKeyAuthCache.CachedApiKey {
        apiKeyAuthCache.get(clientKey, clientSecret)?.let { cached ->
            apiKeyUsageService.recordUsage(cached.keyId, ip)
            return cached
        }

        val apiKey = apiKeyRepository.findByClientKey(clientKey)
            ?: throw InvalidApiKeyException("Invalid API key")
        if (!apiKey.enabled) {
            throw InvalidApiKeyException("API key is disabled")
        }
        if (ApiKeyAuthCache.isExpired(apiKey.expiresAt)) {
            throw ExpiredApiKeyException("API key has expired")
        }
        if (!passwordEncoder.matches(clientSecret, apiKey.clientSecret)) {
            throw InvalidApiKeyException("Invalid API key")
        }

        val validated = ApiKeyAuthCache.CachedApiKey(
            keyId = apiKey.id,
            userId = apiKey.userId,
            expiresAt = apiKey.expiresAt
        )
        apiKeyAuthCache.put(clientKey, clientSecret, validated)
        apiKeyUsageService.recordUsage(apiKey.id, ip)
        return validated
    }
}
