package org.jaqpot.api.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
class Doa(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doa_id_seq")
    @SequenceGenerator(name = "doa_id_seq", sequenceName = "doa_id_seq", allocationSize = 1)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", updatable = false, nullable = false)
    val model: Model,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val method: DoaMethod,

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column
    var rawDoa: ByteArray?,
) : BaseEntity()
