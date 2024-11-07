package org.jaqpot.api.service.authentication.keycloak

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.model.UserDto
import org.keycloak.admin.client.Keycloak
import org.springframework.stereotype.Component

@Component
class KeycloakUserService(private val keycloakConfig: KeycloakConfig, private val keycloakAdminClient: Keycloak) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getUserById(id: String): UserDto? {
        try {
            val user = keycloakAdminClient.realm(keycloakConfig.realm).users().get(id).toRepresentation()
            return UserDto(
                id = user.id,
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                emailVerified = user.isEmailVerified
            )
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by id: $id" }
            return null
        }
    }

    fun getUserByUsername(username: String): UserDto? {
        try {
            val user = keycloakAdminClient.realm(keycloakConfig.realm).users().searchByUsername(username, true).first()
            return UserDto(
                id = user.id,
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                emailVerified = user.isEmailVerified
            )
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by username: $username" }
            return null
        }
    }

    fun getUserByEmail(email: String): UserDto? {
        try {
            val users = keycloakAdminClient.realm(keycloakConfig.realm).users().searchByEmail(email, true)
            if (users.size == 0) {
                return null
            } else if (users.size == 1) {
                val user = users.first()
                return UserDto(
                    id = user.id,
                    username = user.username,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    emailVerified = user.isEmailVerified
                )
            } else {
                logger.error { "Found more than 1 users with the same email" }
                return null
            }
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by email: $email" }
            return null
        }

    }

}
