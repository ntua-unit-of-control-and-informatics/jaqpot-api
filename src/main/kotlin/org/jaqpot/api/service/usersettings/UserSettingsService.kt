package org.jaqpot.api.service.usersettings

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.ws.rs.BadRequestException
import org.jaqpot.api.UserSettingsApiDelegate
import org.jaqpot.api.entity.UserSettings
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.UploadUserAvatar200ResponseDto
import org.jaqpot.api.model.UserSettingsDto
import org.jaqpot.api.repository.UserSettingsRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserId
import org.jaqpot.api.storage.StorageService
import org.jaqpot.api.storage.s3.AWSS3Config
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.TimeUnit

@Service
class UserSettingsService(
    private val authenticationFacade: AuthenticationFacade,
    private val userSettingsRepository: UserSettingsRepository,
    private val storageService: StorageService,
    private val awsS3Config: AWSS3Config,
) : UserSettingsApiDelegate {

    private val usersSettingsCache: Cache<UserId, UserSettings> = Caffeine.newBuilder()
        .expireAfterWrite(4, TimeUnit.HOURS)
        .maximumSize(10_000)
        .build()

    override fun getUserSettings(): ResponseEntity<UserSettingsDto> {
        val isAdmin = authenticationFacade.isAdmin
        val isUpciUser = authenticationFacade.isUpciUser

        val userSettings = usersSettingsCache.get(authenticationFacade.userId) {
            userSettingsRepository.findByUserId(authenticationFacade.userId)
                .orElseGet { UserSettings(userId = authenticationFacade.userId) }
        }

        return ResponseEntity.ok(userSettings.toDto(isUpciUser = isUpciUser, isAdmin = isAdmin))
    }

    fun getUserAvatar(userId: UserId): String? {
        val userSettings = usersSettingsCache.get(userId) {
            userSettingsRepository.findByUserId(userId)
                .orElseGet { UserSettings(userId = userId) }
        }
        return userSettings?.avatarUrl
    }

    override fun saveUserSettings(userSettingsDto: UserSettingsDto): ResponseEntity<UserSettingsDto> {
        val isAdmin = authenticationFacade.isAdmin
        val isUpciUser = authenticationFacade.isUpciUser

        val updatedUserSettings = userSettingsRepository.findByUserId(authenticationFacade.userId)
            .map {
                userSettingsDto.darkMode?.let { darkMode -> it.darkMode = darkMode }
                userSettingsDto.collapseSidebar?.let { collapseSidebar -> it.collapseSidebar = collapseSidebar }
                userSettingsRepository.save(it)
                it
            }
            .orElseGet {
                val userSettings = userSettingsDto.toEntity(userId = authenticationFacade.userId)
                userSettingsRepository.save(userSettings)
            }

        return ResponseEntity.ok(updatedUserSettings.toDto(isUpciUser = isUpciUser, isAdmin = isAdmin))
            .also { usersSettingsCache.put(authenticationFacade.userId, updatedUserSettings) }
    }

    fun uploadUserAvatar(body: MultipartFile): ResponseEntity<UploadUserAvatar200ResponseDto> {
        val extension = when (body.contentType?.toString()) {
            MediaType.IMAGE_JPEG_VALUE -> "jpg"
            MediaType.IMAGE_PNG_VALUE -> "png"
            "image/webp" -> "webp"
            else -> throw BadRequestException("Unsupported image type: ${body.contentType}. Only JPEG, PNG and WebP are supported.")
        }

        if (storageService.storeRawUserAvatar(authenticationFacade.userId, body.bytes, extension)) {
            val avatarUrl = generateUserAvatarUrl(authenticationFacade.userId, extension)
            val userSettings = userSettingsRepository.findByUserId(authenticationFacade.userId)
                .map {
                    it.avatarUrl = avatarUrl
                    it
                }
                .orElseGet {
                    UserSettings(userId = authenticationFacade.userId, avatarUrl = avatarUrl)
                }

            userSettingsRepository.save(userSettings)

            return ResponseEntity.ok(
                UploadUserAvatar200ResponseDto(avatarUrl = avatarUrl)
            ).also { usersSettingsCache.invalidate(authenticationFacade.userId) }
        } else {
            throw JaqpotRuntimeException("Could not upload user avatar")
        }
    }

    private fun generateUserAvatarUrl(userId: String, extension: String): String {
        return "${awsS3Config.cloudfrontImagesDistributionUrl}/avatars/${userId}.$extension"
    }
}
