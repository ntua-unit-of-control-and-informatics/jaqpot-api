package org.jaqpot.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JaqpotApiApplication

fun main(args: Array<String>) {
    runApplication<JaqpotApiApplication>(*args)
}
