package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
class Library(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "library_id_seq")
    @SequenceGenerator(name = "library_id_seq", sequenceName = "library_id_seq", allocationSize = 1)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", updatable = false, nullable = false)
    val model: Model,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val version: String,
) : BaseEntity()
