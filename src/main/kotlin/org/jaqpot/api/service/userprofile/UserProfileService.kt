package org.jaqpot.api.service.userprofile

import org.jaqpot.api.UserApiDelegate
import org.jaqpot.api.model.UserDto
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserProfileService(
    private val authenticationFacade: AuthenticationFacade,
    private val userService: UserService,
) :
    UserApiDelegate {
    override fun getUser(username: String): ResponseEntity<UserDto> {

        return userService.getUserByUsername(username)
            .map {
                ResponseEntity.ok(it.copy(canEdit = authenticationFacade.userId == it.id))
            }
            .orElseGet { ResponseEntity.notFound().build() }
    }
}
