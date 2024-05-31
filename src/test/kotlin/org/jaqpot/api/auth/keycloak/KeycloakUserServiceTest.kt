package org.jaqpot.api.auth.keycloak

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.test.util.ReflectionTestUtils

class KeycloakUserServiceTest {

    private val keycloakConfig = KeycloakConfig(
        "serverUrl",
        "realm",
        "clientId",
        "clientSecret",
    )

    private val userRepresentation: UserRepresentation = mockk()

    private lateinit var keycloakClient: Keycloak
    private lateinit var keycloakUserService: KeycloakUserService

    @BeforeEach
    fun setup() {
        every { userRepresentation.id } returns "id"
        every { userRepresentation.firstName } returns "firstName"
        every { userRepresentation.lastName } returns "lastName"
        keycloakClient = mockk()
        keycloakUserService = KeycloakUserService(keycloakConfig)
        ReflectionTestUtils.setField(keycloakUserService, "keycloakClient", keycloakClient)
    }

    @Test
    fun getUserById() {
        every { keycloakClient.realm("realm").users().get("id").toRepresentation() } returns userRepresentation
        val user = keycloakUserService.getUserById("id")

        assertEquals(user.name, "firstName lastName")

    }

    @Test
    fun getUserByUsername() {
        every {
            keycloakClient.realm("realm").users().searchByUsername("username", true)
        } returns listOf(userRepresentation)
        val user = keycloakUserService.getUserByUsername("username")

        assertEquals(user.name, "firstName lastName")
    }

    @Test
    fun getUserByEmail() {
        every {
            keycloakClient.realm("realm").users().searchByEmail("email", true)
        } returns listOf(userRepresentation)
        val user = keycloakUserService.getUserByEmail("email")

        assertEquals(user.name, "firstName lastName")
    }
}
