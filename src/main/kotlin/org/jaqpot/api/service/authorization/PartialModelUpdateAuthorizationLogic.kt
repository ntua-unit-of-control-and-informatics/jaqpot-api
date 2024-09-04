package org.jaqpot.api.service.authorization

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.model.PartiallyUpdateModelRequestDto
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.organization.OrganizationService
import org.springframework.http.HttpStatus
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component("partialModelUpdateAuthorizationLogic")
class PartialModelUpdateAuthorizationLogic(
    private val modelRepository: ModelRepository,
    private val organizationService: OrganizationService,
    private val authenticationFacade: AuthenticationFacade
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun decide(
        operations: MethodSecurityExpressionOperations,
        modelId: Long,
        partiallyUpdateModelRequestDto: PartiallyUpdateModelRequestDto
    ): Boolean {
        if (authenticationFacade.isAdmin) {
            return true
        }

        val model = modelRepository.findById(modelId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
        }

        val organizationsThatUserCanSee = organizationService.getAllOrganizationsForUser().body
        val organizationsThatUserCanSeeIds = organizationsThatUserCanSee!!.map { it.id }

        if (!partiallyUpdateModelRequestDto.sharedWithOrganizationIds.isNullOrEmpty()) {
            val sharedWithOrganizationIds: List<Long> = partiallyUpdateModelRequestDto.sharedWithOrganizationIds!!

            if (!organizationsThatUserCanSeeIds.containsAll(sharedWithOrganizationIds)) {
                logger.error { "User ${authenticationFacade.userId} attempted to update model with id $modelId and sharedWithOrganizationIds $sharedWithOrganizationIds that they do not have access to" }
                return false
            }
        }

        return authenticationFacade.userId == model.creatorId
    }

}
