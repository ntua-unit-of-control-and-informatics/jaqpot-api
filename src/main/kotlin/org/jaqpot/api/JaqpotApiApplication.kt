package org.jaqpot.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@ConfigurationPropertiesScan("org.jaqpot.api")
@EnableAsync
class JaqpotApiApplication

fun main(args: Array<String>) {
    runApplication<JaqpotApiApplication>(*args)
}
