package org.jaqpot.api.service.authorization

import org.jaqpot.api.error.JaqpotNotFoundException
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.model.ModelService
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component

@Component("predictModelAuthorizationLogic")
class PredictModelAuthorizationLogic(
    private val modelService: ModelService,
    private val authenticationFacade: AuthenticationFacade
) {
    fun decide(operations: MethodSecurityExpressionOperations, modelId: Long): Boolean {
        if (authenticationFacade.isAdmin) {
            return true
        }

        val modelDto =
            this.modelService.getModelById(modelId).body
                ?: throw JaqpotNotFoundException("No model with id $modelId found")


        if (modelDto.visibility == ModelDto.Visibility.PUBLIC) {
            return authenticationFacade.isLoggedIn
        } else if (modelDto.visibility === ModelDto.Visibility.PRIVATE) {
            return modelDto.creator?.id == authenticationFacade.userId
        } else if (modelDto.visibility === ModelDto.Visibility.ORG_SHARED) {
            // TODO finish logic here after creating organizations
            return false
        }

        throw IllegalStateException("Unexpected model visibility ${modelDto.visibility}")
    }
}
