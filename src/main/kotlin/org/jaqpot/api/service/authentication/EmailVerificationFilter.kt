package org.jaqpot.api.service.authentication

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.jaqpot.api.error.ApiErrorResponse
import org.jaqpot.api.model.ErrorCodeDto
import org.springframework.http.HttpStatus

//@Component
@Deprecated("This filter is not used anymore.")
class EmailVerificationFilter(private val authenticationFacade: AuthenticationFacade) : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse?, chain: FilterChain) {
        val authentication = authenticationFacade.authentication

        if (authentication.isAuthenticated) {
            val principal = authentication.principal

            if (principal is org.springframework.security.oauth2.jwt.Jwt) {
                val emailVerified = principal.claims["email_verified"] as Boolean?

                if (emailVerified == false) {
                    sendErrorResponse(response)
                    return
                }
            }
        }

        chain.doFilter(request, response)
    }

    private fun sendErrorResponse(response: ServletResponse?) {
        val httpServletResponse = response as HttpServletResponse
        httpServletResponse.status = HttpServletResponse.SC_UNAUTHORIZED
        httpServletResponse.contentType = "application/json"

        val jsonResponse = ApiErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "You need to verify your email in order to access this endpoint.",
            ErrorCodeDto.EMAIL_NOT_VERIFIED.value
        )

        val objectMapper = ObjectMapper()
        val responseBody = objectMapper.writeValueAsString(jsonResponse)

        httpServletResponse.writer.write(responseBody)
    }
}
