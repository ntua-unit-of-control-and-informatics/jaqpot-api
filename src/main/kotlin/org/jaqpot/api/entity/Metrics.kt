package org.jaqpot.api.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
class Metrics(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metrics_id_seq")
    @SequenceGenerator(name = "metrics_id_seq", sequenceName = "metrics_id_seq", allocationSize = 1)
    val id: Long? = null,

    @OneToOne
    @JoinColumn(name = "model_id", nullable = false)
    val model: Model,

//    // Regression Metrics
    val r2: Float?,
    val mae: Float?,
    val rmse: Float?,
    val rSquaredDiffRZero: Float?,
    val rSquaredDiffRZeroHat: Float?,
    val absDiffRZeroHat: Float?,
    val k: Float?,
    val kHat: Float?,
//
//    // Binary Classification Metrics
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
//
//    // Multiclass Classification Metrics
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

fun main() {
    System.out.println(org.hibernate.Version.getVersionString());

}
