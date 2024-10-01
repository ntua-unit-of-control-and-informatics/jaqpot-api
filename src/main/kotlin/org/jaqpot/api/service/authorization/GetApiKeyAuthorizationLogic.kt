package org.jaqpot.api.service.authorization

import org.jaqpot.api.repository.ApiKeyRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.http.HttpStatus
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component("getApiKeyAuthorizationLogic")
class GetApiKeyAuthorizationLogic(
    private val authenticationFacade: AuthenticationFacade,
    private val apiKeyRepository: ApiKeyRepository
) {
    fun decide(operations: MethodSecurityExpressionOperations, apiClientKey: String): Boolean {
        if (authenticationFacade.isAdmin) {
            return true
        }

        val apiKey = apiKeyRepository.findByClientKey(apiClientKey) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Api key not found"
        )

        return authenticationFacade.userId == apiKey.userId
    }

}
