package org.jaqpot.api.service.authorization

import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.ResponseEntity
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component

@Component("getDatasetAuthorizationLogic")
class GetDatasetAuthorizationLogic(private val authenticationFacade: AuthenticationFacade) {
    fun decide(operations: MethodSecurityExpressionOperations): Boolean {
        val responseEntity = operations.returnObject as ResponseEntity<*>
        if (!responseEntity.statusCode.is2xxSuccessful) {
            return true
        }

        if (authenticationFacade.isAdmin) {
            return true
        }

        val datasetDto: DatasetDto = responseEntity.body as DatasetDto

        return datasetDto.userId == authenticationFacade.userId
    }

}
