package org.jaqpot.api.service.authentication.keycloak

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.*

class KeycloakTokenExchangerTest {

    private val config = KeycloakConfig(
        serverUrl = "https://login.example",
        realm = "jaqpot",
        clientId = "c",
        clientSecret = "s",
        impersonationClientId = "ic",
        impersonationClientSecret = "is"
    )
    private val restTemplate: RestTemplate = mockk()

    private fun unsignedJwt(expiresAt: Instant): String {
        val enc = Base64.getUrlEncoder().withoutPadding()
        val header = enc.encodeToString("""{"alg":"RS256","typ":"JWT"}""".toByteArray())
        val payload = enc.encodeToString(
            """{"sub":"user-1","exp":${expiresAt.epochSecond}}""".toByteArray()
        )
        return "$header.$payload.sig"
    }

    private fun exchanger() = KeycloakTokenExchanger(config, restTemplate)

    @Test
    fun `second call for same user is served from cache`() {
        val token = unsignedJwt(Instant.now().plusSeconds(3600))
        every { restTemplate.postForEntity(any<String>(), any(), eq(Map::class.java)) } returns
            ResponseEntity.ok(mapOf("access_token" to token))

        val ex = exchanger()
        assert(ex.getOrExchangeToken("user-1") == token)
        assert(ex.getOrExchangeToken("user-1") == token)

        verify(exactly = 1) { restTemplate.postForEntity(any<String>(), any(), eq(Map::class.java)) }
    }

    @Test
    fun `nearly expired token is not cached`() {
        val token = unsignedJwt(Instant.now().plusSeconds(10))
        every { restTemplate.postForEntity(any<String>(), any(), eq(Map::class.java)) } returns
            ResponseEntity.ok(mapOf("access_token" to token))

        val ex = exchanger()
        ex.getOrExchangeToken("user-1")
        ex.getOrExchangeToken("user-1")

        verify(exactly = 2) { restTemplate.postForEntity(any<String>(), any(), eq(Map::class.java)) }
    }

    @Test
    fun `evict forces fresh exchange`() {
        val token = unsignedJwt(Instant.now().plusSeconds(3600))
        every { restTemplate.postForEntity(any<String>(), any(), eq(Map::class.java)) } returns
            ResponseEntity.ok(mapOf("access_token" to token))

        val ex = exchanger()
        ex.getOrExchangeToken("user-1")
        ex.evict("user-1")
        ex.getOrExchangeToken("user-1")

        verify(exactly = 2) { restTemplate.postForEntity(any<String>(), any(), eq(Map::class.java)) }
    }
}
