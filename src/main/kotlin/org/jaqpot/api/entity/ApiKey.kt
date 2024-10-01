package org.jaqpot.api.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

// TODO add id for deletion by user and remove @id from key
// allow user to revoke (delete) token

@Entity
class ApiKey(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(nullable = false)
    val clientKey: String,
    @Column(nullable = false)
    val clientSecret: String,
    @Column(nullable = false)
    val userId: String,
    val note: String? = null,
    @Column(nullable = false)
    val expiresAt: OffsetDateTime,
    val enabled: Boolean,
    val lastUsed: OffsetDateTime? = null,
    val lastUsedIp: String? = null
) : BaseEntity()
