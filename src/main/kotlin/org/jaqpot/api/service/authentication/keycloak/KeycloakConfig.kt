package org.jaqpot.api.service.authentication.keycloak

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keycloak")
data class KeycloakConfig(
    val serverUrl: String,
    var realm: String,
    var clientId: String,
    var clientSecret: String,
)
