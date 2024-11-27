package org.jaqpot.api.service.authentication

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.jaqpot.api.model.UserDto
import org.jaqpot.api.service.authentication.keycloak.KeycloakUserService
import org.jaqpot.api.storage.StorageService
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

typealias UserId = String

@Service
class UserCacheService(
    private val keycloakUserService: KeycloakUserService,
    private val storageService: StorageService
) : UserService {

    private val usersCache: Cache<UserId, UserRepresentation> = Caffeine.newBuilder()
        .expireAfterWrite(4, TimeUnit.HOURS)
        .maximumSize(10_000)
        .build()

    override fun getUserById(id: UserId): Optional<UserDto> {
        val userRepresentation = usersCache.get(id) { keycloakUserService.getUserById(id) }

        val userDto = generateUserDto(userRepresentation)

        return Optional.of(userDto)
    }

    override fun getUserByEmail(email: String): Optional<UserDto> {
        val userRepresentation = usersCache.get(email) { keycloakUserService.getUserByEmail(email) }

        val userDto = generateUserDto(userRepresentation)

        return Optional.of(userDto)
    }

    override fun getUserByUsername(username: String): Optional<UserDto> {
        val userRepresentation = usersCache.get(username) { keycloakUserService.getUserByUsername(username) }
        val userDto = generateUserDto(userRepresentation)

        return Optional.of(userDto)
    }

    private fun generateUserDto(userRepresentation: UserRepresentation): UserDto {
        val userAvatar =
            storageService.readRawUserAvatarFromUserId(userRepresentation.id)
        val userDto = UserDto(
            id = userRepresentation.id,
            username = userRepresentation.username,
            firstName = userRepresentation.firstName,
            lastName = userRepresentation.lastName,
            email = userRepresentation.email,
            emailVerified = userRepresentation.isEmailVerified,
            avatar = userAvatar
        )
        return userDto
    }

}
