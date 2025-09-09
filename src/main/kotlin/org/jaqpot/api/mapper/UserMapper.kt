package org.jaqpot.api.mapper

import org.jaqpot.api.model.UserDto
import org.jaqpot.api.service.usersettings.UserSettingsService
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val userSettingsService: UserSettingsService
) {
    
    fun generateUserDto(userRepresentation: UserRepresentation): UserDto {
        return UserDto(
            id = userRepresentation.id,
            username = userRepresentation.username,
            firstName = userRepresentation.firstName,
            lastName = userRepresentation.lastName,
            email = userRepresentation.email,
            emailVerified = userRepresentation.isEmailVerified,
            avatarUrl = userSettingsService.getUserAvatar(userRepresentation.id),
            createdAt = userRepresentation.createdTimestamp?.let { 
                java.time.OffsetDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(it), 
                    java.time.ZoneOffset.UTC
                ) 
            }
        )
    }
}