package org.jaqpot.api.service.authentication

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class EmailVerificationFilter(private val authenticationFacade: AuthenticationFacade) : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse?, chain: FilterChain) {
        val authentication = authenticationFacade.authentication

        if (authentication != null && authentication.isAuthenticated) {
            val principal = authentication.principal

            if (principal is org.springframework.security.oauth2.jwt.Jwt) {
                val emailVerified = principal.claims["email_verified"] as Boolean?

                if (emailVerified == false) {
                    (response as HttpServletResponse).sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "You need to verify your email in order to access this endpoint."
                    )
                    return
                }
            }
        }

        chain.doFilter(request, response)
    }
}
