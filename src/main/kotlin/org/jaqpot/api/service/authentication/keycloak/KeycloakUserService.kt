package org.jaqpot.api.service.authentication.keycloak

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.model.UserDto
import org.keycloak.admin.client.Keycloak
import org.springframework.stereotype.Component
import java.util.*

@Component
class KeycloakUserService(private val keycloakConfig: KeycloakConfig, private val keycloakAdminClient: Keycloak) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getUserById(id: String): Optional<UserDto> {
        try {
            val user = keycloakAdminClient.realm(keycloakConfig.realm).users().get(id).toRepresentation()
            return Optional.of(
                UserDto(
                    id = user.id,
                    username = user.username,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    emailVerified = user.isEmailVerified
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by id: $id" }
            return Optional.empty()
        }
    }

    fun getUserByUsername(username: String): Optional<UserDto> {
        try {
            val user = keycloakAdminClient.realm(keycloakConfig.realm).users().searchByUsername(username, true).first()
            return Optional.of(
                UserDto(
                    id = user.id,
                    username = user.username,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    emailVerified = user.isEmailVerified
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by username: $username" }
            return Optional.empty()
        }
    }

    fun getUserByEmail(email: String): Optional<UserDto> {
        try {
            val users = keycloakAdminClient.realm(keycloakConfig.realm).users().searchByEmail(email, true)
            if (users.size == 0) {
                return Optional.empty()
            } else if (users.size == 1) {
                val user = users.first()
                return Optional.of(
                    UserDto(
                        id = user.id,
                        username = user.username,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        email = user.email,
                        emailVerified = user.isEmailVerified
                    )
                )
            } else {
                logger.error { "Found more than 1 users with the same email" }
                return Optional.empty()
            }
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by email: $email" }
            return Optional.empty()
        }

    }

}
