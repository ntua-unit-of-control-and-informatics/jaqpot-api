package org.jaqpot.api.repository

import org.jaqpot.api.entity.UserSettings
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserSettingsRepository : CrudRepository<UserSettings, Long> {
    fun findByUserId(userId: String): Optional<UserSettings>
}
