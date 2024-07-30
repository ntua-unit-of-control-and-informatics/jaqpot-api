package org.jaqpot.api.entity

import jakarta.persistence.*

@Entity
@Table(name = "organization_models")
class ModelOrganizationAssociation(
    @EmbeddedId
    val id: ModelOrganizationAssociationId,

    @ManyToOne
    @MapsId("modelId")
    @JoinColumn(name = "model_id")
    val model: Model,

    @ManyToOne
    @MapsId("organizationId")
    @JoinColumn(name = "organization_id")
    val organization: Organization,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val associationType: ModelOrganizationAssociationType,

    ) : BaseEntity() {
    constructor(
        model: Model,
        organization: Organization,
        associationType: ModelOrganizationAssociationType
    ) : this(
        ModelOrganizationAssociationId(model.id!!, organization.id!!),
        model,
        organization,
        associationType
    )
}

@Embeddable
data class ModelOrganizationAssociationId(
    @Column(name = "model_id")
    val modelId: Long,

    @Column(name = "organization_id")
    val organizationId: Long
)


