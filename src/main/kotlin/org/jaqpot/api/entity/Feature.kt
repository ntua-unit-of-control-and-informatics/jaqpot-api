package org.jaqpot.api.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Pattern
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

    @Pattern(
        regexp = "^[a-zA-Z][a-zA-Z0-9_-]*\$",
        message = "Feature key must start with a letter and can only contain letters, digits, hyphens, and underscores."
    )
    @Column(nullable = false)
    val key: String,

    @Column(nullable = false)
    var name: String,

    @Size(min = 3, max = 5000)
    @Column(columnDefinition = "TEXT")
    var description: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val featureDependency: FeatureDependency,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var featureType: FeatureType,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    val meta: Map<String, Any>?,

    @Column
    val visible: Boolean?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var possibleValues: List<FeaturePossibleValue>? = null,

    ) : BaseEntity()
