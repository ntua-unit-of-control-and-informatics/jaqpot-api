package org.jaqpot.api.service.authentication.password

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class JaqpotPasswordEncoder : PasswordEncoder {

    companion object {
        private val passwordEncoder = BCryptPasswordEncoder()
    }

    override fun encode(password: String): String {
        return passwordEncoder.encode(password)
    }

    override fun matches(password: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(password, encodedPassword)
    }
}
