package org.jaqpot.api.service.authorization

import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.model.ModelDto
import org.jaqpot.api.model.ModelVisibilityDto
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.ResponseEntity
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component

@Component("getModelAuthorizationLogic")
class GetModelAuthorizationLogic(private val authenticationFacade: AuthenticationFacade) {
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
            // TODO finish logic here after creating organizations
            return false
        }

        throw JaqpotRuntimeException("Unexpected model visibility ${modelDto.visibility}")
    }
}
