package org.jaqpot.api.service.authentication

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component


@Component
class AuthenticationFacade {

    companion object {
        const val ADMIN_ROLE = "admin"
        const val ENTERPRISE_ROLE = "enterprise"
        const val PRO_ROLE = "pro"
    }

    val isLoggedIn: Boolean get() = authentication !is AnonymousAuthenticationToken
    val isAdmin: Boolean get() = authentication.authorities.any { it.authority == ADMIN_ROLE }
    val isEnterpriseUser: Boolean get() = authentication.authorities.any { it.authority == ENTERPRISE_ROLE }
    val isProUser: Boolean get() = authentication.authorities.any { it.authority == PRO_ROLE }
    val authentication: Authentication
        get() = SecurityContextHolder.getContext().authentication

    val userId: String get() = authentication.name

}


