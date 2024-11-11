package org.jaqpot.api.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
class Scores(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scores_id_seq")
    @SequenceGenerator(name = "scores_id_seq", sequenceName = "scores_id_seq", allocationSize = 1)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    val model: Model,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val scoreType: ScoreType,

    @Column(nullable = false)
    val yName: String,

    @Column
    val labels: Array<String>?,

    val folds: Int?,

    // Regression Scores
    val r2: Float?,
    val mae: Float?,
    val rmse: Float?,

    // Binary Classification Metrics
    val accuracy: Float?,
    val balancedAccuracy: Float?,
    val precision: FloatArray?,
    val recall: FloatArray?,
    val f1Score: FloatArray?,
    val jaccard: FloatArray?,
    val matthewsCorrCoef: Float?,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val confusionMatrix: Array<FloatArray>?,

    // Multiclass Classification Metrics
    val multiClassAccuracy: Float?,
    val multiClassBalancedAccuracy: Float?,
    val multiClassPrecision: FloatArray?,
    val multiClassRecall: FloatArray?,
    val multiClassF1Score: FloatArray?,
    val multiClassJaccard: FloatArray?,
    val multiClassMatthewsCorrCoef: Float?,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val multiClassConfusionMatrix: Array<FloatArray>?
) : BaseEntity()

