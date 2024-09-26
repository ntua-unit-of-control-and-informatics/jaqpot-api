package org.jaqpot.api.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.OffsetDateTime

@Entity
class ApiKey(
    @Id
    val key: String,
    val userId: String,
    val expiresAt: OffsetDateTime? = null,
    val lastUsed: OffsetDateTime? = null,
    val lastUsedIp: String? = null
) : BaseEntity()