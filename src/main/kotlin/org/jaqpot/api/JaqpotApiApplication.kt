package org.jaqpot.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("org.jaqpot.api")
class JaqpotApiApplication

fun main(args: Array<String>) {
    runApplication<JaqpotApiApplication>(*args)
}
