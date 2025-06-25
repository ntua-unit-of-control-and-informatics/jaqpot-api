package org.jaqpot.api.service.authentication

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.jaqpot.api.model.UserDto
import org.jaqpot.api.service.authentication.keycloak.KeycloakUserService
import org.jaqpot.api.service.usersettings.UserSettingsService
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

typealias UserId = String

@Service
class UserCacheService(
    private val keycloakUserService: KeycloakUserService,
    private val userSettingsService: UserSettingsService
) : UserService {

    private val usersCache: Cache<String, UserRepresentation?> = Caffeine.newBuilder()
        .expireAfterWrite(4, TimeUnit.HOURS)
        .maximumSize(10_000)
        .build()

    private val usernameCache: Cache<String, UserRepresentation?> = Caffeine.newBuilder()
        .expireAfterWrite(4, TimeUnit.HOURS)
        .maximumSize(10_000)
        .build()

    override fun getUserById(id: String): Optional<UserDto> {
        val userRepresentation = usersCache.get(id) { userId -> keycloakUserService.getUserById(userId) }

        return Optional.ofNullable(generateUserDto(userRepresentation))
    }

    override fun getUserByEmail(email: String): Optional<UserDto> {
        val userRepresentation = usersCache.get(email) { keycloakUserService.getUserByEmail(email) }

        return Optional.ofNullable(generateUserDto(userRepresentation))
    }

    override fun getUserByUsername(username: String): Optional<UserDto> {
        val userRepresentation = usernameCache.get(username) { keycloakUserService.getUserByUsername(username) }

        return Optional.ofNullable(generateUserDto(userRepresentation))
    }

    private fun generateUserDto(userRepresentation: UserRepresentation?): UserDto? {
        if (userRepresentation == null) {
            return null
        }
        return UserDto(
            id = userRepresentation.id,
            username = userRepresentation.username,
            firstName = userRepresentation.firstName,
            lastName = userRepresentation.lastName,
            email = userRepresentation.email,
            emailVerified = userRepresentation.isEmailVerified,
            avatarUrl = userSettingsService.getUserAvatar(userRepresentation.id)
        )
    }
}
