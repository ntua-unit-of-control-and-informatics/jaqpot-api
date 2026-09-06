package org.jaqpot.api.service.authentication.apikey

import io.mockk.mockk
import io.mockk.verify
import org.jaqpot.api.repository.ApiKeyRepository
import org.junit.jupiter.api.Test
import java.util.*

class ApiKeyUsageServiceTest {

    private val repository: ApiKeyRepository = mockk(relaxed = true)
    private val self: ApiKeyUsageService = mockk(relaxed = true)
    private val usageService = ApiKeyUsageService(repository, self)
    private val keyId = UUID.randomUUID()

    @Test
    fun `first recordUsage delegates to async persist, second within window is throttled`() {
        usageService.recordUsage(keyId, "1.2.3.4")
        usageService.recordUsage(keyId, "1.2.3.4")

        verify(exactly = 1) { self.persistUsage(keyId, "1.2.3.4") }
    }

    @Test
    fun `null key id is ignored`() {
        usageService.recordUsage(null, "1.2.3.4")

        verify(exactly = 0) { self.persistUsage(any(), any()) }
    }

    @Test
    fun `persistUsage writes lastUsed to repository`() {
        usageService.persistUsage(keyId, "5.6.7.8")

        verify(exactly = 1) { repository.updateLastUsed(keyId, any(), "5.6.7.8") }
    }
}
