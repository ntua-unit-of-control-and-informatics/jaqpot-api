package org.jaqpot.api.service.authorization

import org.aopalliance.intercept.MethodInvocation
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.function.Supplier

@Component
class PreAuthorizationManager() : AuthorizationManager<MethodInvocation> {
    override fun check(authentication: Supplier<Authentication>, invocation: MethodInvocation): AuthorizationDecision {
        return AuthorizationDecision(true)
    }
}
