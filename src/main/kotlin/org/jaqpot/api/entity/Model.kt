package org.jaqpot.api.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes

@Entity
class Model(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_id_seq")
    @SequenceGenerator(name = "model_id_seq", sequenceName = "model_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @Column(updatable = false)
    val legacyId: String?,

    @Column(nullable = false, updatable = false)
    val creatorId: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    val meta: Map<String, Any>?,

    @Size(min = 3, max = 255)
    @Column(nullable = false)
    var name: String,

    @Size(min = 3, max = 15000)
    @Column(columnDefinition = "TEXT")
    var description: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ModelType,

    @Column(nullable = false)
    val jaqpotpyVersion: String,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    val libraries: MutableList<Library>,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderColumn(name = "sort_order")
    @SQLRestriction("feature_dependency = 'DEPENDENT'")
    val dependentFeatures: MutableList<Feature>,

    @OneToMany(mappedBy = "model", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderColumn(name = "sort_order")
    @SQLRestriction("feature_dependency = 'INDEPENDENT'")
    val independentFeatures: MutableList<Feature>,

    @ManyToMany
    @JoinTable(
        name = "organization_models",
        joinColumns = [JoinColumn(name = "model_id")],
        inverseJoinColumns = [JoinColumn(name = "organization_id")]
    )
    val organizations: MutableSet<Organization> = mutableSetOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var visibility: ModelVisibility,

    @Column(updatable = false)
    val legacyPredictionService: String?,

    val pretrained: Boolean?,

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(nullable = false)
    val actualModel: ByteArray,
) : BaseEntity()
