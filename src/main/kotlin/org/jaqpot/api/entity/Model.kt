package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
class Model(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,
    val isPublic: Boolean = false
) : BaseEntity()
