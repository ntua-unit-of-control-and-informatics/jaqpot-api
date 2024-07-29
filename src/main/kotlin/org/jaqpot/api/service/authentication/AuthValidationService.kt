package org.jaqpot.api.service.authentication

import org.jaqpot.api.AuthApiDelegate
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AuthValidationService : AuthApiDelegate {
    override fun validateJWT(): ResponseEntity<Unit> {
        return ResponseEntity.ok().build()
    }
}
