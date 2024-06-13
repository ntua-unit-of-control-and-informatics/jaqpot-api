package org.jaqpot.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
class OrganizationInvitation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID?,

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val userEmail: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", updatable = false, nullable = false)
    val organization: Organization,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrganizationInvitationStatus = OrganizationInvitationStatus.PENDING,

    @Column(updatable = false, nullable = false)
    val expirationDate: LocalDateTime

) : BaseEntity()
