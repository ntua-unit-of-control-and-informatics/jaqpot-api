package org.jaqpot.api.service.authentication.keycloak

import org.jaqpot.api.error.JaqpotRuntimeException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate


@Component
class KeycloakTokenExchanger(private val keycloakConfig: KeycloakConfig) {
    /**
     * Exchange a user token for an impersonation token
     * @see <a href="https://www.keycloak.org/docs/24.0.3/securing_apps/index.html#direct-naked-impersonation">https://www.keycloak.org/docs/24.0.3/securing_apps/index.html#direct-naked-impersonation</a>
     */
    fun exchangeToken(userId: String): String {
        val restTemplate = RestTemplate()

        // Set headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        // Create body with required parameters
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", keycloakConfig.impersonationClientId)
        body.add("client_secret", keycloakConfig.impersonationClientSecret)
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
        body.add("requested_subject", userId) // The user to impersonate

        // Create the request entity
        val requestEntity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(body, headers)

        // Send the request
        val response: ResponseEntity<Map<*, *>?> = restTemplate.postForEntity(
            "${keycloakConfig.serverUrl}/realms/${keycloakConfig.realm}/protocol/openid-connect/token",
            requestEntity,
            Map::class.java
        )

        // Extract the access token from the response
        if (response.statusCode.is2xxSuccessful && response.body != null) {
            return response.body!!["access_token"].toString()
        } else {
            throw JaqpotRuntimeException("Failed to exchange token")
        }
    }
}
