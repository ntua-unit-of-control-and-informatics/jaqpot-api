package org.jaqpot.api.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
class ModelTransformer(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_transformer_id_seq")
    @SequenceGenerator(name = "model_transformer_id_seq", sequenceName = "model_transformer_id_seq", allocationSize = 1)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", updatable = false, nullable = false)
    val model: Model,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val transformerType: ModelTransformerType,

    @Column(nullable = false)
    val name: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "jsonb")
    val config: Map<String, Any> = emptyMap(),
) : BaseEntity()
