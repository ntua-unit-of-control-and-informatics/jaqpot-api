package org.jaqpot.api.service.authorization

import org.jaqpot.api.error.JaqpotNotFoundException
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.model.ModelVisibilityDto
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


        if (modelDto.visibility == ModelVisibilityDto.PUBLIC) {
            return authenticationFacade.isLoggedIn
        } else if (modelDto.visibility === ModelVisibilityDto.PRIVATE) {
            return modelDto.creator?.id == authenticationFacade.userId
        } else if (modelDto.visibility === ModelVisibilityDto.ORG_SHARED) {
            if (modelDto.creator?.id == authenticationFacade.userId) {
                return true
            }

            val userIdsFromSharedOrganizations =
                getUserIdsFromSharedOrganizations(modelDto)

            return userIdsFromSharedOrganizations.contains(authenticationFacade.userId)
        }

        throw IllegalStateException("Unexpected model visibility ${modelDto.visibility}")
    }

    private fun getUserIdsFromSharedOrganizations(modelDto: ModelDto) =
        modelDto.organizations?.flatMap { it -> it.userIds ?: emptyList() } ?: emptyList()
}
