package org.jaqpot.api.service.authorization

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.model.ModelVisibilityDto
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.ResponseEntity
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component

@Component("getModelAuthorizationLogic")
class GetModelAuthorizationLogic(
    private val authenticationFacade: AuthenticationFacade,
    private val organizationRepository: OrganizationRepository
) {
    fun decide(operations: MethodSecurityExpressionOperations): Boolean {
        val responseEntity = operations.returnObject as ResponseEntity<*>
        if (!responseEntity.statusCode.is2xxSuccessful) {
            return true
        }

        if (authenticationFacade.isAdmin) {
            return true
        }

        val modelDto: ModelDto = responseEntity.body as ModelDto

        if (modelDto.visibility == ModelVisibilityDto.PUBLIC) {
            return true
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

        throw JaqpotRuntimeException("Unexpected model visibility ${modelDto.visibility}")
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
