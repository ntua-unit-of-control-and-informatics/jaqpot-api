package org.jaqpot.api.service.authorization

import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.HttpStatus
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component("modelUpdateAuthorizationLogic")
class ModelUpdateAuthorizationLogic(
    private val modelRepository: ModelRepository,
    private val authenticationFacade: AuthenticationFacade
) {
    fun decide(operations: MethodSecurityExpressionOperations, modelId: Long): Boolean {
        if (authenticationFacade.isAdmin) {
            return true
        }

        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        return authenticationFacade.userId == model.creatorId
    }

}
