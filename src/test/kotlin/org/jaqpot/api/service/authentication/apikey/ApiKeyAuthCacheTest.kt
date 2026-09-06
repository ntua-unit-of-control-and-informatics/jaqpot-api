package org.jaqpot.api.service.authentication.apikey

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class ApiKeyAuthCacheTest {

    private val cache = ApiKeyAuthCache()
    private val keyId = UUID.randomUUID()

    private fun entry(expiresAt: OffsetDateTime) = ApiKeyAuthCache.CachedApiKey(
        keyId = keyId,
        userId = "user-1",
        expiresAt = expiresAt
    )

    @Test
    fun `put then get returns entry`() {
        cache.put("jq_k", "s", entry(OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1)))

        val got = cache.get("jq_k", "s")

        assertNotNull(got)
        assertEquals(keyId, got!!.keyId)
        assertEquals("user-1", got.userId)
    }

    @Test
    fun `wrong secret misses`() {
        cache.put("jq_k", "s", entry(OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1)))

        assertNull(cache.get("jq_k", "other-secret"))
    }

    @Test
    fun `expired entry is never served`() {
        // Directly seeding an already-expired entry must still read as a miss.
        cache.put("jq_k", "s", entry(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)))

        assertNull(cache.get("jq_k", "s"))
    }

    @Test
    fun `isExpired keeps key valid for its whole expiry day`() {
        val todayStart = OffsetDateTime.now(ZoneOffset.UTC)
            .withHour(0).withMinute(0).withSecond(0).withNano(0)

        assertFalse(ApiKeyAuthCache.isExpired(todayStart.plusHours(1)))
        assertFalse(ApiKeyAuthCache.isExpired(todayStart))
        assertTrue(ApiKeyAuthCache.isExpired(todayStart.minusNanos(1)))
    }

    @Test
    fun `evictByClientKey removes entries without touching other keys`() {
        val future = OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1)
        cache.put("jq_a", "s1", entry(future))
        cache.put("jq_a", "s2", entry(future))
        cache.put("jq_b", "s3", entry(future))

        cache.evictByClientKey("jq_a")

        assertNull(cache.get("jq_a", "s1"))
        assertNull(cache.get("jq_a", "s2"))
        assertNotNull(cache.get("jq_b", "s3"))
    }
}
