package org.jaqpot.api.service.authorization

import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.stereotype.Component

@Component("authz")
class ModelAuthorizationLogic {
    fun decide(operations: MethodSecurityExpressionOperations): AuthorizationDecision {

        return AuthorizationDecision(false)
    }
}
