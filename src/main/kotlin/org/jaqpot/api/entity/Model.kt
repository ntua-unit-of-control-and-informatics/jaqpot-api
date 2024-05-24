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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    val meta: Map<String, Any>,

    val public: Boolean?,

    val type: String?,

    val jaqpotpyVersion: String,

    @OneToMany(mappedBy = "model")
    val libraries: List<Library>,

    @OneToMany(mappedBy = "model")
    @OrderColumn(name = "order")
    @SQLRestriction("featureType = 'DEPENDENT'")
    val dependentFeatures: List<Feature>,

    @OneToMany(mappedBy = "model")
    @OrderColumn(name = "order")
    @SQLRestriction("featureType = 'INDEPENDENT'")
    val independentFeatures: List<Feature>,

    val reliability: Int?,

    val pretrained: Boolean?,

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    val actualModel: ByteArray,
) : BaseEntity()
