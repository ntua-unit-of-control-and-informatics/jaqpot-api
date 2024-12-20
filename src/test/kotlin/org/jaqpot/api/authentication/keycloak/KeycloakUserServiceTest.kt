package org.jaqpot.api.authentication.keycloak

import io.mockk.every
import io.mockk.mockk
import org.jaqpot.api.service.authentication.keycloak.KeycloakConfig
import org.jaqpot.api.service.authentication.keycloak.KeycloakUserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation

class KeycloakUserServiceTest {

    private val keycloakConfig = KeycloakConfig(
        "serverUrl",
        "realm",
        "clientId",
        "clientSecret",
        "impersonationClientId",
        "impersonationClientSecret",
    )

    private val userRepresentation: UserRepresentation = mockk()

    private lateinit var keycloakClient: Keycloak
    private lateinit var keycloakUserService: KeycloakUserService

    @BeforeEach
    fun setup() {
        every { userRepresentation.id } returns "id"
        every { userRepresentation.username } returns "username"
        every { userRepresentation.firstName } returns "firstName"
        every { userRepresentation.lastName } returns "lastName"
        every { userRepresentation.email } returns "email@email.com"
        every { userRepresentation.isEmailVerified } returns true
        keycloakClient = mockk()
        keycloakUserService = KeycloakUserService(keycloakConfig, keycloakClient)
    }

    @Test
    fun getUserById() {
        every { keycloakClient.realm("realm").users().get("id").toRepresentation() } returns userRepresentation
        val user = keycloakUserService.getUserById("id")

        assertEquals(user?.username, "username")
    }

    @Test
    fun getUserByUsername() {
        every {
            keycloakClient.realm("realm").users().searchByUsername("username", true)
        } returns listOf(userRepresentation)
        val user = keycloakUserService.getUserByUsername("username")

        assertEquals(user?.username, "username")
    }

    @Test
    fun getUserByEmail() {
        every {
            keycloakClient.realm("realm").users().searchByEmail("email", true)
        } returns listOf(userRepresentation)
        val user = keycloakUserService.getUserByEmail("email")

        assertEquals(user?.username, "username")
    }
}
