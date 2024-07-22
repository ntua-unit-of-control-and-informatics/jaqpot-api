package org.jaqpot.api.service.authorization

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.entity.OrganizationVisibility
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.HttpStatus
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component("getAllAssociatedModelsAuthorizationLogic")
class GetAllAssociatedModelsAuthorizationLogic(
    private val organizationRepository: OrganizationRepository,
    private val authenticationFacade: AuthenticationFacade
) {
    fun decide(operations: MethodSecurityExpressionOperations, orgName: String): Boolean {
        val organization = organizationRepository.findByName(orgName).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Organization with name $orgName not found")
        }

        if (authenticationFacade.isAdmin) {
            return true
        }

        if (organization.visibility == OrganizationVisibility.PUBLIC) {
            return true
        } else if (organization.visibility === OrganizationVisibility.PRIVATE) {
            return organization.creatorId == authenticationFacade.userId || (userBelongsToOrganization(
                organization,
                organization.userIds
            ))
        }

        throw JaqpotRuntimeException("Unexpected org visibility ${organization.visibility}")
    }

    private fun userBelongsToOrganization(
        organization: Organization,
        userIds: MutableSet<String>
    ): Boolean {
        if (organization.userIds.isEmpty()) {
            return false;
        }

        return userIds.contains(authenticationFacade.userId)
    }

}
