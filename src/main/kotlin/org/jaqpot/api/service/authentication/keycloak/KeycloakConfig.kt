package org.jaqpot.api.service.authentication.keycloak

import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.ClientBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import java.util.concurrent.TimeUnit


@ConfigurationProperties(prefix = "keycloak")
data class KeycloakConfig(
    val serverUrl: String,
    val realm: String,
    val clientId: String,
    val clientSecret: String,
    val impersonationClientId: String,
    val impersonationClientSecret: String
) {

    @Bean
    fun customizedResteasyClient(): Client {
        val httpClient = createHttpClient()

        val engine = ApacheHttpClient43Engine(httpClient)
        return (ClientBuilder.newBuilder() as ResteasyClientBuilder)
            .httpEngine(engine)
            .connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(7000, TimeUnit.MILLISECONDS)
            .connectionTTL(-1, TimeUnit.MILLISECONDS)
            .disableTrustManager()
            .build()
    }

    private fun createHttpClient(): CloseableHttpClient {
        val cm = PoolingHttpClientConnectionManager()
        cm.validateAfterInactivity = 1000
        cm.maxTotal = 200
        cm.defaultMaxPerRoute = 20
        return HttpClients.custom()
            .setConnectionManager(cm)
            .evictExpiredConnections()
            .evictIdleConnections(10000, TimeUnit.MILLISECONDS)
            .setRetryHandler(DefaultHttpRequestRetryHandler.INSTANCE)
            .build()
    }

    @Bean("keycloakAdminClient")
    fun keycloakAdminClient(keycloakConfig: KeycloakConfig): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakConfig.serverUrl)
            .realm(keycloakConfig.realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(keycloakConfig.clientId)
            .clientSecret(keycloakConfig.clientSecret)
            .resteasyClient(customizedResteasyClient())
            .build()
    }
}
