package org.jaqpot.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

@SpringBootApplication
@ConfigurationPropertiesScan("org.jaqpot.api")
@EnableAsync
@EnableMethodSecurity
@EnableCaching
class JaqpotApiApplication

fun main(args: Array<String>) {
    runApplication<JaqpotApiApplication>(*args)
}
