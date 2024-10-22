package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
@Table(name = "organization_models")
class ModelOrganizationAssociation(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_models_id_seq")
    @SequenceGenerator(
        name = "organization_models_id_seq",
        sequenceName = "organization_models_id_seq",
        allocationSize = 1
    )
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    val model: Model,

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    val organization: Organization,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val associationType: ModelOrganizationAssociationType

) : BaseEntity()



