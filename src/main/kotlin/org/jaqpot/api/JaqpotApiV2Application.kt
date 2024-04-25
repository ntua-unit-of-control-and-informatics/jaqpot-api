package org.jaqpot.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JaqpotApiV2Application

fun main(args: Array<String>) {
    runApplication<JaqpotApiV2Application>(*args)
}
