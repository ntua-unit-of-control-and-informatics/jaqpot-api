package org.jaqpot.api.service.authentication.keycloak

import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate


@Component
class KeycloakTokenExchanger(private val keycloakConfig: KeycloakConfig) {
    fun exchangeToken(userId: String): String {
        val restTemplate = RestTemplate()

        // Set headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED


        // Create body with required parameters
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", keycloakConfig.clientId)
        body.add("client_secret", keycloakConfig.clientSecret)
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
        body.add("requested_subject", userId) // The user to impersonate


        // Create the request entity
        val requestEntity: HttpEntity<MultiValueMap<String, String>> = HttpEntity(body, headers)


        // Send the request
        val response: ResponseEntity<Map<*, *>?> = restTemplate.exchange(
            "${keycloakConfig.serverUrl}/auth/realms/${keycloakConfig.realm}/protocol/openid-connect/token",
            HttpMethod.POST,
            requestEntity,
            Map::class.java
        )


        // Extract the access token from the response
        if (response.statusCode.is2xxSuccessful && response.body != null) {
            return response.body!!["access_token"].toString()
        } else {
            throw RuntimeException("Failed to exchange token")
        }
    }
}
