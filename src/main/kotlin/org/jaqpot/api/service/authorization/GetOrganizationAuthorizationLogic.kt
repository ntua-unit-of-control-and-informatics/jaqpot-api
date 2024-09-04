package org.jaqpot.api.service.authorization

import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.model.OrganizationDto
import org.jaqpot.api.model.OrganizationVisibilityDto
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.ResponseEntity
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component

@Component("getOrganizationAuthorizationLogic")
class GetOrganizationAuthorizationLogic(private val authenticationFacade: AuthenticationFacade) {
    fun decide(operations: MethodSecurityExpressionOperations): Boolean {
        val responseEntity = operations.returnObject as ResponseEntity<*>
        if (!responseEntity.statusCode.is2xxSuccessful) {
            return true
        }

        if (authenticationFacade.isAdmin) {
            return true
        }

        val organizationDto: OrganizationDto = responseEntity.body as OrganizationDto

        if (organizationDto.visibility == OrganizationVisibilityDto.PUBLIC) {
            return true
        } else if (organizationDto.visibility === OrganizationVisibilityDto.PRIVATE) {
            return organizationDto.creator!!.id == authenticationFacade.userId || (userBelongsToOrganization(
                organizationDto.organizationMembers?.map { it.userId }
            ))
        }

        throw JaqpotRuntimeException("Unexpected org visibility ${organizationDto.visibility}")
    }

    private fun userBelongsToOrganization(
        userIds: List<String>?
    ): Boolean {
        if (userIds.isNullOrEmpty()) {
            return false;
        }

        return userIds!!.contains(authenticationFacade.userId)
    }

}
