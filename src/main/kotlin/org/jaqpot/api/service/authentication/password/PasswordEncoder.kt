package org.jaqpot.api.service.authentication.password

interface PasswordEncoder {
    fun encode(password: String): String
    fun matches(password: String, encodedPassword: String): Boolean
}
