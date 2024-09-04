package org.jaqpot.api.service.authorization

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.error.JaqpotNotFoundException
import org.jaqpot.api.model.ModelVisibilityDto
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.model.ModelService
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component

@Component("predictModelAuthorizationLogic")
class PredictModelAuthorizationLogic(
    private val modelService: ModelService,
    private val organizationRepository: OrganizationRepository,
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

            val modelOrganizations =
                organizationRepository.findAllById(modelDto.sharedWithOrganizations?.map { it.id }).toList()

            val allAllowedUserIds = allAllowedUserIdsForModel(modelOrganizations)

            return allAllowedUserIds.contains(authenticationFacade.userId)
        }

        throw IllegalStateException("Unexpected model visibility ${modelDto.visibility}")
    }

    private fun allAllowedUserIdsForModel(modelOrganizations: List<Organization>): List<String> {
        val creatorIdsFromModelOrganizations =
            modelOrganizations.map { it.creatorId }
        val userIdsFromModelOrganizations =
            modelOrganizations.flatMap { organization -> organization.organizationMembers.map { it.userId } }

        val allAllowedUserIds = creatorIdsFromModelOrganizations + userIdsFromModelOrganizations
        return allAllowedUserIds
    }
}
