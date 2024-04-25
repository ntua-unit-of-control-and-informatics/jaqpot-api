package org.jaqpot.api.entity

import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@MappedSuperclass
open class BaseEntity(
    @CreationTimestamp
    private val createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    private val updatedAt: LocalDateTime? = null
)
