package org.jaqpot.api.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Entity
class Lead(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lead_id_seq")
    @SequenceGenerator(name = "lead_id_seq", sequenceName = "lead_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = true)
    val name: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: LeadStatus = LeadStatus.PENDING,

    ) : BaseEntity()

enum class LeadStatus {
    PENDING,
    APPROVED,
    DENIED
}
