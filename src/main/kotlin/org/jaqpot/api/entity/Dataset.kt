package org.jaqpot.api.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
class Dataset(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataset_id_seq")
    @SequenceGenerator(name = "dataset_id_seq", sequenceName = "dataset_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", updatable = false, nullable = false)
    val model: Model,

    @Column(nullable = false)
    val userId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: DatasetType = DatasetType.PREDICTION,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DatasetStatus = DatasetStatus.CREATED,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val entryType: DatasetEntryType = DatasetEntryType.ARRAY,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input", columnDefinition = "jsonb")
    val input: List<Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", columnDefinition = "jsonb")
    var result: List<Any>? = null,

    @Size(min = 3, max = 15000)
    @Column(columnDefinition = "TEXT")
    var failureReason: String?,

    var executedAt: OffsetDateTime? = null,

    var executionFinishedAt: OffsetDateTime? = null
) : BaseEntity()
