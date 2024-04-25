package org.jaqpot.api.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class OAuth2ResourceServerController {

    @GetMapping("/")
    fun index(@AuthenticationPrincipal jwt: Jwt): String {
        return String.format("Hello, %s!", jwt.subject)
    }

    @GetMapping("/message")
    fun message(): String {
        return "secret message"
    }
}
