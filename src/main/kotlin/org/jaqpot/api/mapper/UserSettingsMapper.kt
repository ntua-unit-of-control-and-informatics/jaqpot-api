package org.jaqpot.api.mapper

import org.jaqpot.api.entity.UserSettings
import org.jaqpot.api.model.UserSettingsDto

fun UserSettings.toDto(isUpciUser: Boolean, isAdmin: Boolean): UserSettingsDto {
    return UserSettingsDto(
        id = this.id,
        darkMode = this.darkMode,
        collapseSidebar = this.collapseSidebar,
        isAdmin = isAdmin,
        isUpci = isUpciUser
    )
}

fun UserSettingsDto.toEntity(userId: String): UserSettings {
    return UserSettings(
        id = this.id,
        userId = userId,
        darkMode = this.darkMode,
        collapseSidebar = this.collapseSidebar
    )
}
