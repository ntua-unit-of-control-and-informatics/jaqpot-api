package org.jaqpot.api.service.authentication.keycloak

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.model.UserDto
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.stereotype.Component
import java.util.*

@Component
class KeycloakUserService(private val keycloakConfig: KeycloakConfig) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val keycloakAdminClient = KeycloakBuilder.builder()
        .serverUrl(keycloakConfig.serverUrl)
        .realm(keycloakConfig.realm)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(keycloakConfig.clientId)
        .clientSecret(keycloakConfig.clientSecret)
        .build()

    fun getUserById(id: String): Optional<UserDto> {
        try {
            val user = keycloakAdminClient.realm(keycloakConfig.realm).users().get(id).toRepresentation()
            return Optional.of(
                UserDto(
                    user.id,
                    user.username,
                    user.email,
                    user.isEmailVerified
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
            return Optional.of(UserDto(user.id, user.username, user.email, user.isEmailVerified))
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
                        user.id,
                        user.username,
                        user.email,
                        user.isEmailVerified
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
