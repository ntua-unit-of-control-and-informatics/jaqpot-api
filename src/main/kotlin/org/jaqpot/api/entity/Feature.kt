package org.jaqpot.api.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
class Feature(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feature_id_seq")
    @SequenceGenerator(name = "feature_id_seq", sequenceName = "feature_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @ManyToOne
    @JoinColumn(name = "model_id")
    val model: Model,

    @Column
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column
    val featureType: FeatureType,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    val meta: Map<String, Any>?,

    @Column
    val visible: Boolean?,

    ) : BaseEntity()
