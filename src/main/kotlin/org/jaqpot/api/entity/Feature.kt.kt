package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
class `Feature.kt`(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long = 0,

    @Column(name = "feature_id", unique = true, nullable = false)
    val featureId: String,

    @Embedded
    val meta: `Meta.kt`,

    val visible: Boolean
) : BaseEntity()
