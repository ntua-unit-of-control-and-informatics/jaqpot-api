package org.jaqpot.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.Duration

/**
 * Provides a [RestTemplate] with bounded connect/read timeouts.
 *
 * A bare `RestTemplate()` uses infinite connect and read timeouts. When a downstream service
 * (e.g. the QSAR Toolbox backend) accepts a connection but never responds, the calling thread
 * blocks forever. Because prediction requests run on the shared `@Async` pool, a handful of such
 * hung calls permanently saturate the pool and silently stall all dataset offloading.
 */
@Configuration
class RestTemplateConfig {

    companion object {
        private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(10)
        private val READ_TIMEOUT: Duration = Duration.ofSeconds(60)
    }

    @Bean
    fun restTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(CONNECT_TIMEOUT)
        factory.setReadTimeout(READ_TIMEOUT)
        return RestTemplate(factory)
    }
}
