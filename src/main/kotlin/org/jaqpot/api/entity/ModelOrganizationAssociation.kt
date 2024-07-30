package org.jaqpot.api.entity

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "organization_models")
class ModelOrganizationAssociation(
    @AttributeOverride(name = "associationType", column = Column(name = "association_type"))
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

    ) : BaseEntity() {
    constructor(
        model: Model,
        organization: Organization,
        associationType: ModelOrganizationAssociationType
    ) : this(
        ModelOrganizationAssociationId(model.id!!, organization.id!!, associationType),
        model,
        organization,
    )
}

@Embeddable
class ModelOrganizationAssociationId(
    @Column(name = "model_id")
    val modelId: Long,

    @Column(name = "organization_id")
    val organizationId: Long,

    @Enumerated(EnumType.STRING)
    val associationType: ModelOrganizationAssociationType? = null
) : Serializable


