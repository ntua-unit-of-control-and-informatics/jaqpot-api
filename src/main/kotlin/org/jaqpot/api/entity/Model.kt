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
    val id: Long = 0,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    val meta: Map<String, Any>,

    val public: Boolean,

    val type: String,

    val jaqpotpyVersion: String,

    @OneToMany(mappedBy = "model")
    val libraries: Set<Library>,

    @OneToMany(mappedBy = "model")
    @SQLRestriction("featureType = 'DEPENDENT'")
    val dependentFeatures: Set<Feature>,

    @OneToMany(mappedBy = "model")
    @SQLRestriction("featureType = 'INDEPENDENT'")
    val independentFeatures: Set<Feature>,

    val reliability: Int,

    val pretrained: Boolean,

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    val actualModel: ByteArray,
) : BaseEntity()
