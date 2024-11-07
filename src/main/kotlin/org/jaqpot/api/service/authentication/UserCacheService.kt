package org.jaqpot.api.service.authentication

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.jaqpot.api.model.UserDto
import org.jaqpot.api.service.authentication.keycloak.KeycloakUserService
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

typealias UserId = String

@Service
class UserCacheService(
    private val keycloakUserService: KeycloakUserService,
) : UserService {

    private val usersCache: Cache<UserId, UserDto> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .maximumSize(10_000)
        .build()

    override fun getUserById(id: UserId): Optional<UserDto> {
        return Optional.ofNullable(usersCache.get(id) { userId -> keycloakUserService.getUserById(userId) })
    }

    override fun getUserByEmail(email: String): Optional<UserDto> {
        return Optional.ofNullable(keycloakUserService.getUserByEmail(email))
    }

}
