package org.jaqpot.api.service.authentication.keycloak

import io.github.oshai.kotlinlogging.KotlinLogging
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Component

@Component
class KeycloakUserService(private val keycloakConfig: KeycloakConfig, private val keycloakAdminClient: Keycloak) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getUserById(id: String): UserRepresentation? {
        try {
            return keycloakAdminClient.realm(keycloakConfig.realm).users().get(id).toRepresentation()
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by id: $id" }
            return null
        }
    }

    fun getUserByUsername(username: String): UserRepresentation? {
        try {
            return keycloakAdminClient.realm(keycloakConfig.realm).users().searchByUsername(username, true).first()
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by username: $username" }
            return null
        }
    }

    fun getUserByEmail(email: String): UserRepresentation? {
        try {
            val users = keycloakAdminClient.realm(keycloakConfig.realm).users().searchByEmail(email, true)
            when (users.size) {
                0 -> {
                    return null
                }

                1 -> {
                    return users.first()
                }

                else -> {
                    logger.error { "Found more than 1 users with the same email" }
                    return null
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by email: $email" }
            return null
        }

    }

    fun getUsersPaginated(firstResult: Int, maxResults: Int): List<UserRepresentation> {
        try {
            return keycloakAdminClient.realm(keycloakConfig.realm).users()
                .list(firstResult * maxResults, maxResults)
                .sortedByDescending { it.createdTimestamp }
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve paginated users" }
            return emptyList()
        }
    }

}
