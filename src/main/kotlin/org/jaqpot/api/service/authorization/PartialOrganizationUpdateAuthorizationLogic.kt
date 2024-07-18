package org.jaqpot.api.service.authorization

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.HttpStatus
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component("partialOrganizationUpdateAuthorizationLogic")
class PartialOrganizationUpdateAuthorizationLogic(
    private val organizationRepository: OrganizationRepository,
    private val authenticationFacade: AuthenticationFacade
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun decide(
        operations: MethodSecurityExpressionOperations,
        organizationId: Long,
    ): Boolean {
        if (authenticationFacade.isAdmin) {
            return true
        }

        val organization = organizationRepository.findById(organizationId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Organization with id $organizationId not found")
        }

        return authenticationFacade.userId == organization.creatorId
    }

}
