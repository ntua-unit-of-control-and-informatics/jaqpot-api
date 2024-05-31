package org.jaqpot.api.auth.keycloak

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.auth.UserService
import org.jaqpot.api.model.UserDto
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class KeycloakUserService(private val keycloakConfig: KeycloakConfig) : UserService {

    private val keycloakClient = KeycloakBuilder.builder()
        .serverUrl(keycloakConfig.serverUrl)
        .realm(keycloakConfig.realm)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(keycloakConfig.clientId)
        .clientSecret(keycloakConfig.clientSecret)
        .build()

    override fun getUserById(id: String): UserDto {
        val user = keycloakClient.realm(keycloakConfig.realm).users().get(id).toRepresentation()
        return UserDto(user.id, "${user.firstName} ${user.lastName}")
    }

    override fun getUserByUsername(username: String): UserDto {
        val user = keycloakClient.realm(keycloakConfig.realm).users().searchByUsername(username, true).first()
        return UserDto(user.id, "${user.firstName} ${user.lastName}")
    }

    override fun getUserByEmail(email: String): UserDto {
        val user = keycloakClient.realm(keycloakConfig.realm).users().searchByEmail(email, true).first()
        return UserDto(user.id, "${user.firstName} ${user.lastName}")
    }
}
