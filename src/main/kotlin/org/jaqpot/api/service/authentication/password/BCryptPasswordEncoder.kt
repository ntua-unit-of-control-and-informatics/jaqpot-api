package org.jaqpot.api.service.authentication.password

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class BCryptPasswordEncoder : PasswordEncoder {

    companion object {
        private val passwordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()
        private val logger = KotlinLogging.logger {}
    }

    override fun encode(password: String): String {
        return passwordEncoder.encode(password)
    }

    override fun matches(password: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(password, encodedPassword)
    }
}
