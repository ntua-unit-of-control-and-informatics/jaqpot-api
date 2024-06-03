package org.jaqpot.api.service.authentication

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component


@Component
class AuthenticationFacade {
    val authentication: Authentication
        get() = SecurityContextHolder.getContext().authentication

    val userId: String get() = authentication.name

}


