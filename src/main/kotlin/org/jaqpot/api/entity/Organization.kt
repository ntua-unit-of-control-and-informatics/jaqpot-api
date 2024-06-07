package org.jaqpot.api.entity;

import jakarta.persistence.*
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Entity
class Organization(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_id_seq")
    @SequenceGenerator(name = "organization_id_seq", sequenceName = "organization_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @Size(min = 3, max = 255)
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "[\\w-_]+")
    val name: String,

    @Column(nullable = false)
    val creatorId: String,

    @Size(min = 3, max = 2000)
    @Column
    val description: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "organization_users",
        joinColumns = [JoinColumn(name = "organization_id")]
    )
    @Column(name = "user_id", nullable = false)
    val userIds: Set<String> = mutableSetOf(),


    @ManyToMany
    @JoinTable(
        name = "organization_models",
        joinColumns = [JoinColumn(name = "organization_id")],
        inverseJoinColumns = [JoinColumn(name = "model_id")]
    )
    val models: MutableSet<Model> = mutableSetOf(),

    @Column
    val contactEmail: String? = null,

    @Column
    val contactPhone: String? = null,

    @Column
    val website: String? = null,

    @Column
    val address: String? = null,

    ) : BaseEntity()
