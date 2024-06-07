package org.jaqpot.api.integration

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.parsing.Parser
import io.restassured.specification.RequestSpecification
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.PostgreSQLContainer


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [AbstractIntegrationTest.Initializer::class])
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    companion object {
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
                "jdbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/${TEST_DB_NAME}"

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

        RestAssured.defaultParser = Parser.JSON
        val spec: RequestSpecification = RequestSpecBuilder()
            .setBaseUri(keycloakUrl).build()

        val accessToken = given()
            .spec(spec)
            .contentType(ContentType.URLENC)
            .formParam("client_id", "jaqpot-local-test")
            .formParam("grant_type", "password")
            .formParam("username", "jaqpot")
            .formParam("password", "jaqpot")
            .post("/realms/jaqpot-local/protocol/openid-connect/token")
            .then()
            .statusCode(200)
            .extract().path<String>("access_token")

        return accessToken
    }


}
