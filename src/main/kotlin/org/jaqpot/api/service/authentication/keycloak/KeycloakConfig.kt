package org.jaqpot.api.service.authentication.keycloak

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean

@ConfigurationProperties(prefix = "keycloak")
data class KeycloakConfig(
    val serverUrl: String,
    var realm: String,
    var clientId: String,
    var clientSecret: String
) {


    @Bean("keycloakAdminClient")
    fun keycloakAdminClient(keycloakConfig: KeycloakConfig): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakConfig.serverUrl)
            .realm(keycloakConfig.realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(keycloakConfig.clientId)
            .clientSecret(keycloakConfig.clientSecret)
            .build()
    }
}
