package org.jaqpot.api.service.authentication.apikey

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Caches successful API key validations so that every request does not pay for a database
 * read, a BCrypt check and a Keycloak token exchange.
 *
 * Correctness guarantees (this is why the cache is safe for auth data):
 *
 * - Only *successful* validations are cached. Unknown keys, wrong secrets, disabled keys and
 *   expired keys always fall through to the database, so a key that is created, re-enabled or
 *   fixed takes effect immediately.
 * - Each entry expires at `min(now + VALIDATION_TTL, key.expiresAt)` via a custom [Expiry], so a
 *   cached key can never be honored past its own expiration time. A defensive re-check of
 *   `expiresAt` on every read covers any clock/expiry race.
 * - Mutations ([ApiKeyService.updateApiKey]/[ApiKeyService.deleteApiKey]) evict every entry for
 *   the affected `clientKey`, so disables and deletes take effect immediately.
 *
 * Worst-case staleness: a key disabled or deleted *directly in the database* (bypassing the
 * service) is still honored for at most [VALIDATION_TTL_MINUTES].
 */
@Component
class ApiKeyAuthCache {

    companion object {
        const val VALIDATION_TTL_MINUTES = 5L
        private const val MAX_SIZE = 10_000L

        fun credentialsHash(clientKey: String, clientSecret: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$clientKey:$clientSecret".toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        /**
         * Single source of truth for "has this key expired". Mirrors the pre-cache rule exactly:
         * a key is expired once its `expiresAt` is before the start of the current UTC day, i.e. a
         * key remains valid for the whole of its expiry day. Both the cache and the database path
         * must use this predicate so they never disagree.
         */
        fun isExpired(expiresAt: OffsetDateTime, now: OffsetDateTime = now()): Boolean {
            val todayStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0)
            return expiresAt.isBefore(todayStart)
        }

        fun now(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
    }

    data class CacheKey(val clientKey: String, val credentialsHash: String)

    data class CachedApiKey(
        val keyId: UUID?,
        val userId: String,
        val expiresAt: OffsetDateTime
    )

    private val cache: Cache<CacheKey, CachedApiKey> = Caffeine.newBuilder()
        .maximumSize(MAX_SIZE)
        .expireAfter(object : Expiry<CacheKey, CachedApiKey> {
            override fun expireAfterCreate(key: CacheKey, value: CachedApiKey, currentTime: Long): Long {
                val ttlNanos = TimeUnit.MINUTES.toNanos(VALIDATION_TTL_MINUTES)
                val untilExpiryNanos = try {
                    java.time.Duration.between(now(), value.expiresAt).toNanos().coerceAtLeast(0)
                } catch (e: Exception) {
                    0L
                }
                return minOf(ttlNanos, untilExpiryNanos)
            }

            override fun expireAfterUpdate(
                key: CacheKey,
                value: CachedApiKey,
                currentTime: Long,
                currentDuration: Long
            ): Long = currentDuration

            override fun expireAfterRead(
                key: CacheKey,
                value: CachedApiKey,
                currentTime: Long,
                currentDuration: Long
            ): Long = currentDuration
        })
        .build()

    /**
     * Returns the cached validation, or null on miss *or* when the entry reached the key's own
     * `expiresAt` (defensive re-check with the same predicate as the database path; the [Expiry]
     * above should already have removed it).
     */
    fun get(clientKey: String, clientSecret: String): CachedApiKey? {
        val key = CacheKey(clientKey, credentialsHash(clientKey, clientSecret))
        val cached = cache.getIfPresent(key) ?: return null
        if (isExpired(cached.expiresAt)) {
            cache.invalidate(key)
            return null
        }
        return cached
    }

    fun put(clientKey: String, clientSecret: String, value: CachedApiKey) {
        if (isExpired(value.expiresAt)) return
        cache.put(CacheKey(clientKey, credentialsHash(clientKey, clientSecret)), value)
    }

    fun evictByClientKey(clientKey: String) {
        cache.asMap().keys.removeIf { it.clientKey == clientKey }
    }

    // Visible for tests.
    internal fun size(): Long = cache.estimatedSize()
}
