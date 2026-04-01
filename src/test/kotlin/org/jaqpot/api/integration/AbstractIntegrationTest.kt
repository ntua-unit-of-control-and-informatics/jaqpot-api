package org.jaqpot.api.integration

import com.fasterxml.jackson.databind.ObjectMapper
import dasniko.testcontainers.keycloak.KeycloakContainer
import java.io.IOException
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.PostgreSQLContainer


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [AbstractIntegrationTest.Initializer::class])
abstract class AbstractIntegrationTest {

    companion object {
        init {
            // RestAssured's Groovy proxy detection calls ProxySelector.getDefault() and can NPE
            // on macOS / Java 21 when the system proxy selector returns entries with null keys.
            // Replace the default selector with a direct (no-proxy) implementation.
            ProxySelector.setDefault(object : ProxySelector() {
                override fun select(uri: URI?) = listOf(Proxy.NO_PROXY)
                override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {}
            })
        }

        const val TEST_DB_NAME = "jaqpot-test"
        const val TEST_DB_USERNAME = "test"
        const val TEST_DB_PASSWORD = "test"
        val postgreSQLContainer = PostgreSQLContainer("postgres:alpine3.19")
            .apply {
                withDatabaseName(TEST_DB_NAME)
                withUsername(TEST_DB_USERNAME)
                withPassword(TEST_DB_PASSWORD)
            }

        val keycloakContainer =
            KeycloakContainer("quay.io/keycloak/keycloak:24.0.3")
                .withRealmImportFile("/test-realm-export.json")
    }


    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            postgreSQLContainer.start()
            keycloakContainer.start()

            val keycloakUrl = "http://${keycloakContainer.host}:${keycloakContainer.firstMappedPort}"
            val dbUrl =
                "jdbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/$TEST_DB_NAME"

            TestPropertyValues.of(
                "spring.datasource.url= $dbUrl",
                "spring.datasource.username=${postgreSQLContainer.username}",
                "spring.datasource.password=${postgreSQLContainer.password}",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=${keycloakUrl}/realms/jaqpot-local"
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    fun getJaqpotUserAccessToken(): String {
        val keycloakUrl = "http://${keycloakContainer.host}:${keycloakContainer.firstMappedPort}"
        val body = "client_id=jaqpot-local-test&grant_type=password&username=jaqpot&password=jaqpot"

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$keycloakUrl/realms/jaqpot-local/protocol/openid-connect/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        check(response.statusCode() == 200) { "Keycloak token request failed with status ${response.statusCode()}: ${response.body()}" }

        return ObjectMapper().readTree(response.body()).get("access_token").asText()
    }


}
