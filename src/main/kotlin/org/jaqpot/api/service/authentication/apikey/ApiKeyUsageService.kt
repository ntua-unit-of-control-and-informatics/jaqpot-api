package org.jaqpot.api.service.authentication.apikey

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.jaqpot.api.repository.ApiKeyRepository
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Records API key usage (`lastUsed`/`lastUsedIp`) off the request path.
 *
 * Previously every successful validation issued a synchronous `UPDATE`, adding a database write
 * (and row lock) to every API-key-authenticated request. Now the write is:
 *
 * - asynchronous (backed by the bounded `applicationTaskExecutor`, so it can never block the
 *   request thread), and
 * - throttled: at most one write per key per [WRITE_THROTTLE_MINUTES]. `lastUsed` is
 *   informational/analytics data, minute-level precision is more than enough.
 *
 * Note: [persistUsage] must be invoked through the Spring proxy for `@Async` to apply, hence
 * the self-injection below (a plain internal call would run synchronously on the caller thread).
 */
@Service
class ApiKeyUsageService(
    private val apiKeyRepository: ApiKeyRepository,
    @Lazy private val self: ApiKeyUsageService
) {
    companion object {
        const val WRITE_THROTTLE_MINUTES = 15L
    }

    private val lastWrite: Cache<UUID, OffsetDateTime> = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(WRITE_THROTTLE_MINUTES, TimeUnit.MINUTES)
        .build()

    fun recordUsage(keyId: UUID?, ip: String) {
        if (keyId == null) return
        // Absent (or throttled out) -> this request owns the write for the next window.
        if (lastWrite.getIfPresent(keyId) != null) return
        lastWrite.put(keyId, OffsetDateTime.now(ZoneOffset.UTC))
        self.persistUsage(keyId, ip)
    }

    @Async
    open fun persistUsage(keyId: UUID, ip: String) {
        apiKeyRepository.updateLastUsed(keyId, OffsetDateTime.now(ZoneOffset.UTC), ip)
    }
}
