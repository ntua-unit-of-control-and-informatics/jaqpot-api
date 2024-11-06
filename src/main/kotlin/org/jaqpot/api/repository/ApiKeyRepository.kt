package org.jaqpot.api.repository

import jakarta.transaction.Transactional
import org.jaqpot.api.entity.ApiKey
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.*

interface ApiKeyRepository : CrudRepository<ApiKey, UUID> {
    fun findAllByUserId(userId: String): List<ApiKey>
    fun findByClientKey(clientKey: String): ApiKey?

    @Modifying
    @Transactional
    @Query("UPDATE ApiKey ak SET ak.lastUsed = :date, ak.lastUsedIp = :ip WHERE ak.id = :id")
    fun updateLastUsed(@Param("id") id: UUID?, @Param("date") date: OffsetDateTime, @Param("ip") ip: String)

}
