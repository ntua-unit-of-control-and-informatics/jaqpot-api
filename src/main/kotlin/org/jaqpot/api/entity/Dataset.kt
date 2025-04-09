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
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", updatable = false, nullable = false)
    val model: Model,

    @Column
    val name: String? = null,

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
    var input: List<Any>? = emptyList(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", columnDefinition = "jsonb")
    var result: List<Any>? = emptyList(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_types", columnDefinition = "jsonb")
    var resultTypes: Map<String, String>? = emptyMap(),

    @Size(min = 3, max = 15000)
    @Column(columnDefinition = "TEXT")
    var failureReason: String?,

    var executedAt: OffsetDateTime? = null,

    var executionFinishedAt: OffsetDateTime? = null
) : BaseEntity() {
    /**
     * This is to avoid querying s3 for non-existing results.
     */
    fun shouldHaveResult(): Boolean {
        return (this.type == DatasetType.PREDICTION && this.status == DatasetStatus.SUCCESS) || this.type == DatasetType.CHAT
    }
}


