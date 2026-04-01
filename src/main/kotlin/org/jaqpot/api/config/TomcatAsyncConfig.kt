package org.jaqpot.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class TomcatAsyncConfig : WebMvcConfigurer {
    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer.setDefaultTimeout(180_000) // 3 minutes
    }
}
