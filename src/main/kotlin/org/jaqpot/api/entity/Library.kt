package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
class Library(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "library_id_seq")
    @SequenceGenerator(name = "library_id_seq", sequenceName = "library_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @ManyToOne
    @JoinColumn(name = "model_id", updatable = false)
    val model: Model,

    @Column
    val name: String,

    @Column
    val version: String,
) : BaseEntity()
