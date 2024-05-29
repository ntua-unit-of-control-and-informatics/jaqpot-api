package org.jaqpot.api.integration

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
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            postgreSQLContainer.start()

            val dbUrl =
                "jdbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/${TEST_DB_NAME}"

            TestPropertyValues.of(
                "spring.datasource.url= $dbUrl",
                "spring.datasource.username=${postgreSQLContainer.username}",
                "spring.datasource.password=${postgreSQLContainer.password}",
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}
