package org.jaqpot.api.service.authentication

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component


@Component
class AuthenticationFacade {

    companion object {
        const val ADMIN_ROLE = "admin"
    }

    val isLoggedIn: Boolean get() = authentication !is AnonymousAuthenticationToken
    val isAdmin: Boolean get() = authentication.authorities.any { it.authority == ADMIN_ROLE }
    val authentication: Authentication
        get() = SecurityContextHolder.getContext().authentication

    val userId: String get() = authentication.name

}


