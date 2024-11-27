package org.jaqpot.api.service.usersettings

import org.jaqpot.api.UserSettingsApiDelegate
import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.UserSettingsDto
import org.jaqpot.api.repository.UserSettingsRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.jaqpot.api.storage.StorageService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserSettingsService(
    val authenticationFacade: AuthenticationFacade, // do not make private, cache is using this field
    private val userSettingsRepository: UserSettingsRepository,
    private val storageService: StorageService,
    private val userService: UserService
) : UserSettingsApiDelegate {

    @Cacheable(value = [CacheKeys.USER_SETTINGS], key = "#root.target.authenticationFacade.userId")
    override fun getUserSettings(): ResponseEntity<UserSettingsDto> {
        val isAdmin = authenticationFacade.isAdmin
        val isUpciUser = authenticationFacade.isUpciUser

        return userSettingsRepository.findByUserId(authenticationFacade.userId)
            .map {
                val userAvatar = storageService.readRawUserAvatarFromUserSettings(it)
                ResponseEntity.ok(it.toDto(isUpciUser = isUpciUser, isAdmin = isAdmin, rawAvatar = userAvatar))
            }
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
                val userAvatar = if (userSettingsDto.rawAvatar != null) {
                    it.rawAvatar = userSettingsDto.rawAvatar
                    if (storageService.storeRawUserAvatar(it)) {
                        it.rawAvatar = null
                    }
                    userSettingsDto.rawAvatar
                } else {
                    it.rawAvatar
                }
                userSettingsRepository.save(it)
                ResponseEntity.ok(it.toDto(isUpciUser = isUpciUser, isAdmin = isAdmin, rawAvatar = userAvatar))
            }
            .orElseGet {
                val userSettings = userSettingsDto.toEntity(userId = authenticationFacade.userId)
                val userAvatar = if (userSettingsDto.rawAvatar != null) {
                    userSettings.rawAvatar = userSettingsDto.rawAvatar
                    if (storageService.storeRawUserAvatar(userSettings)) {
                        userSettings.rawAvatar = null
                    }
                    userSettingsDto.rawAvatar
                } else {
                    null
                }
                userSettingsRepository.save(userSettings)
                ResponseEntity.ok(
                    userSettings.toDto(
                        isUpciUser = isUpciUser,
                        isAdmin = isAdmin,
                        rawAvatar = userAvatar
                    )
                )
            }
    }


}
