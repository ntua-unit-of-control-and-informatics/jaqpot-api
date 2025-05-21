package org.jaqpot.api.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "dataset_prediction_cache",
    indexes = [
        Index(
            name = "idx_dataset_prediction_cache_model_hash",
            columnList = "model_id,input_hash"
        )
    ]
)
class DatasetPredictionCache(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "model_id", nullable = false)
    val modelId: String,

    @Column(name = "input_hash", nullable = false)
    val inputHash: String,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    val dataset: Dataset
)
