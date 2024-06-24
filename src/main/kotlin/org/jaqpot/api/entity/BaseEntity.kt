package org.jaqpot.api.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@MappedSuperclass
open class BaseEntity(
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    val createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: OffsetDateTime? = null
)
