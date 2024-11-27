package org.jaqpot.api.service.authentication

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.jaqpot.api.model.UserDto
import org.jaqpot.api.service.authentication.keycloak.KeycloakUserService
import org.jaqpot.api.storage.StorageService
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

typealias UserId = String

@Service
class UserCacheService(
    private val keycloakUserService: KeycloakUserService,
    private val storageService: StorageService
) : UserService {

    private val usersCache: Cache<UserId, UserDto> = Caffeine.newBuilder()
        .expireAfterWrite(4, TimeUnit.HOURS)
        .maximumSize(10_000)
        .build()

    override fun getUserById(id: UserId): Optional<UserDto> {
        val userFromCache = usersCache.getIfPresent(id)
        if (userFromCache != null) {
            return Optional.of(userFromCache)
        }

        val userRepresentation = keycloakUserService.getUserById(id) ?: return Optional.empty()
        val userAvatar = storageService.readRawUserAvatarFromUserId(id)
        val userDto = UserDto(
            id = userRepresentation.id,
            username = userRepresentation.username,
            firstName = userRepresentation.firstName,
            lastName = userRepresentation.lastName,
            email = userRepresentation.email,
            emailVerified = userRepresentation.isEmailVerified,
            avatar = userAvatar
        )


        return Optional.of(userDto).also { usersCache.put(id, userDto) }
    }

    override fun getUserByEmail(email: String): Optional<UserDto> {
        val userFromCache = usersCache.getIfPresent(email)
        if (userFromCache != null) {
            return Optional.of(userFromCache)
        }

        val userRepresentation = keycloakUserService.getUserByEmail(email) ?: return Optional.empty()
        val userAvatar = storageService.readRawUserAvatarFromUserId(userRepresentation.id)
        val userDto = UserDto(
            id = userRepresentation.id,
            username = userRepresentation.username,
            firstName = userRepresentation.firstName,
            lastName = userRepresentation.lastName,
            email = userRepresentation.email,
            emailVerified = userRepresentation.isEmailVerified,
            avatar = userAvatar
        )

        return Optional.of(userDto).also { usersCache.put(userRepresentation.email, userDto) }
    }

}
