package org.jaqpot.api.service.authorization

import org.jaqpot.api.entity.ModelVisibility
import org.jaqpot.api.entity.Organization
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.HttpStatus
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component("downloadModelAuthorizationLogic")
class DownloadModelAuthorizationLogic(
    private val authenticationFacade: AuthenticationFacade,
    private val organizationRepository: OrganizationRepository,
    private val modelRepository: ModelRepository
) {
    fun decide(operations: MethodSecurityExpressionOperations, modelId: Long): Boolean {
        if (authenticationFacade.isAdmin) {
            return true
        }

        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        if (model.visibility == ModelVisibility.PUBLIC) {
            return true
        } else if (model.visibility === ModelVisibility.PRIVATE) {
            return model.creatorId == authenticationFacade.userId
        } else if (model.visibility === ModelVisibility.ORG_SHARED) {
            if (model.creatorId == authenticationFacade.userId) {
                return true
            }

            val modelOrganizations =
                organizationRepository.findAllById(model.sharedWithOrganizations?.map { it.organization.id }).toList()

            val allAllowedUserIds = allAllowedUserIdsForModel(modelOrganizations)

            return allAllowedUserIds.contains(authenticationFacade.userId)
        }

        throw JaqpotRuntimeException("Unexpected model visibility ${model.visibility}")
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
