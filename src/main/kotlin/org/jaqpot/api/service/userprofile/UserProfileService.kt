package org.jaqpot.api.service.userprofile

import org.jaqpot.api.UserApiDelegate
import org.jaqpot.api.model.UserDto
import org.jaqpot.api.service.authentication.UserService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserProfileService(private val userService: UserService) : UserApiDelegate {
    override fun getUser(userId: String): ResponseEntity<UserDto> {
        return userService.getUserById(userId)
            .map { ResponseEntity.ok(it) }
            .orElseGet { ResponseEntity.notFound().build() }
    }
}
