package org.jaqpot.api.service.authorization

import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.HttpStatus
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component("organizationInviteAuthorizationLogic")
class OrganizationInviteAuthorizationLogic(
    private val organizationRepository: OrganizationRepository,
    private val authenticationFacade: AuthenticationFacade
) {
    fun decide(operations: MethodSecurityExpressionOperations, orgName: String): Boolean {
        if (authenticationFacade.isAdmin) {
            return true
        }

        val organization = organizationRepository.findByName(orgName).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Organization with name $orgName not found")
        }

        return authenticationFacade.userId == organization.creatorId
    }
}
