package org.jaqpot.api.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.annotations.SQLRestriction

@Entity
class Organization(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_id_seq")
    @SequenceGenerator(name = "organization_id_seq", sequenceName = "organization_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @Size(min = 3, max = 255)
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "[\\w-_]+")
    var name: String,

    @Column(nullable = false, updatable = false)
    val creatorId: String,

    @Size(min = 3, max = 15000)
    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "organization_users",
        joinColumns = [JoinColumn(name = "organization_id")]
    )
    @Column(name = "user_id", nullable = false)
    val userIds: MutableSet<String> = mutableSetOf(),

    @OneToMany(mappedBy = "organization", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("association_type = 'AFFILIATION'")
    val affiliatedModels: MutableSet<ModelOrganizationAssociation> = mutableSetOf(),

    @OneToMany(mappedBy = "organization", cascade = [CascadeType.ALL], orphanRemoval = true)
    @SQLRestriction("association_type = 'SHARE'")
    val sharedModels: MutableSet<ModelOrganizationAssociation> = mutableSetOf(),

    @OneToMany(mappedBy = "organization", orphanRemoval = true)
    val organizationInvitations: MutableList<OrganizationInvitation>,

    @Column(nullable = false)
    var contactEmail: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var visibility: OrganizationVisibility = OrganizationVisibility.PUBLIC,

    @Column
    val contactPhone: String? = null,

    @Column
    val website: String? = null,

    @Column
    val address: String? = null,

    ) : BaseEntity()
