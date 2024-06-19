package org.jaqpot.api.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
class Feature(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feature_id_seq")
    @SequenceGenerator(name = "feature_id_seq", sequenceName = "feature_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    val model: Model,

    @Column(nullable = false)
    val name: String,

    @Size(min = 3, max = 5000)
    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val featureDependency: FeatureDependency,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val featureType: FeatureType,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    val meta: Map<String, Any>?,

    @Column
    val visible: Boolean?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val possibleValues: List<String>? = null,

    ) : BaseEntity()
