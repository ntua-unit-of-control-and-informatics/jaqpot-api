package org.jaqpot.api.service.authorization

import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.authorization.method.MethodInvocationResult
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.function.Supplier

@Component
class PostAuthorizationManager :
    AuthorizationManager<MethodInvocationResult> {
    override fun check(
        authentication: Supplier<Authentication>?,
        `object`: MethodInvocationResult?
    ): AuthorizationDecision {
        return AuthorizationDecision(true)
    }
}
