package org.jaqpot.api.service.usersettings

import org.jaqpot.api.UserSettingsApiDelegate
import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.UserSettingsDto
import org.jaqpot.api.repository.UserSettingsRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserSettingsService(
    val authenticationFacade: AuthenticationFacade,
    private val userSettingsRepository: UserSettingsRepository
) : UserSettingsApiDelegate {

    @Cacheable(value = [CacheKeys.USER_SETTINGS], key = "#root.target.authenticationFacade.userId")
    override fun getUserSettings(): ResponseEntity<UserSettingsDto> {
        val isAdmin = authenticationFacade.isAdmin
        val isUpciUser = authenticationFacade.isUpciUser

        return userSettingsRepository.findByUserId(authenticationFacade.userId)
            .map { ResponseEntity.ok(it.toDto(isUpciUser = isUpciUser, isAdmin = isAdmin)) }
            .orElseGet { ResponseEntity.ok(UserSettingsDto(isUpciUser = isUpciUser, isAdmin = isAdmin)) }
    }

    @CacheEvict(value = [CacheKeys.USER_SETTINGS], key = "#root.target.authenticationFacade.userId")
    override fun saveUserSettings(userSettingsDto: UserSettingsDto): ResponseEntity<UserSettingsDto> {
        val isAdmin = authenticationFacade.isAdmin
        val isUpciUser = authenticationFacade.isUpciUser

        return userSettingsRepository.findByUserId(authenticationFacade.userId)
            .map {
                userSettingsDto.darkMode?.let { darkMode -> it.darkMode = darkMode }
                userSettingsDto.collapseSidebar?.let { collapseSidebar -> it.collapseSidebar = collapseSidebar }
                userSettingsRepository.save(it)
                ResponseEntity.ok(it.toDto(isUpciUser = isUpciUser, isAdmin = isAdmin))
            }
            .orElseGet {
                val userSettings = userSettingsDto.toEntity(userId = authenticationFacade.userId)
                userSettingsRepository.save(userSettings)
                ResponseEntity.ok(userSettings.toDto(isUpciUser = isUpciUser, isAdmin = isAdmin))
            }
    }


}
