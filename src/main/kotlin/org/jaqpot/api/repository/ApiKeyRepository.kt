package org.jaqpot.api.repository

import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.entity.ApiKey
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.CrudRepository
import java.time.OffsetDateTime

interface ApiKeyRepository : CrudRepository<ApiKey, String> {
    @Cacheable(cacheNames = [CacheKeys.ALL_API_KEYS])
    fun findAllByExpiresAtIsAfter(date: OffsetDateTime): List<ApiKey>
}
