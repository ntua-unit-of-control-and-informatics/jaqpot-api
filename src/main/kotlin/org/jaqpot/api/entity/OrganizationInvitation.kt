package org.jaqpot.api.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
class OrganizationInvitation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID?,

    @Column
    var userId: String?,

    @Column(nullable = false)
    val userEmail: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", updatable = false, nullable = false)
    val organization: Organization,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrganizationInvitationStatus = OrganizationInvitationStatus.PENDING,

    @Column(updatable = false, nullable = false)
    val expirationDate: OffsetDateTime

) : BaseEntity()
