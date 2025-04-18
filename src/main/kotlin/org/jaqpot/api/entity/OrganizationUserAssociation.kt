package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
@Table(name = "organization_users")
class OrganizationUserAssociation(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_users_id_seq")
    @SequenceGenerator(
        name = "organization_users_id_seq",
        sequenceName = "organization_users_id_seq",
        allocationSize = 1
    )
    val id: Long? = null,

    @Column(nullable = false)
    val userId: String,

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    val organization: Organization,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val associationType: OrganizationUserAssociationType

) : BaseEntity()



