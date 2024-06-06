package org.jaqpot.api.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes

@Entity
class Model(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_id_seq")
    @SequenceGenerator(name = "model_id_seq", sequenceName = "model_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @Column(nullable = false)
    val userId: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    val meta: Map<String, Any>?,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val description: String,

    // TODO create specific model types
    val type: String?,

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

    val reliability: Int?,

    val pretrained: Boolean?,

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(nullable = false)
    val actualModel: ByteArray,
) : BaseEntity()
