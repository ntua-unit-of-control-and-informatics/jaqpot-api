package org.jaqpot.api.entity

import jakarta.persistence.*
import org.hibernate.annotations.Type

@Entity
class Metrics(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metrics_id_seq")
    @SequenceGenerator(name = "metrics_id_seq", sequenceName = "metrics_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @OneToOne
    @JoinColumn(name = "model_id", nullable = false)
    val model: Model,

    // Regression Metrics
    val r2: Float?,
    val mae: Float?,
    val rmse: Float?,
    val rSquaredDiffRZero: Float?,
    val rSquaredDiffRZeroHat: Float?,
    val absDiffRZeroHat: Float?,
    val k: Float?,
    val kHat: Float?,

    // Binary Classification Metrics
    val accuracy: Float?,
    val balancedAccuracy: Float?,
    @Column(columnDefinition = "float8[]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val precision: FloatArray?,
    @Column(columnDefinition = "float8[]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val recall: FloatArray?,
    @Column(columnDefinition = "float8[]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val f1Score: FloatArray?,
    @Column(columnDefinition = "float8[]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val jaccard: FloatArray?,
    val matthewsCorrCoef: Float?,
    @Column(columnDefinition = "float8[][]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val confusionMatrix: Array<FloatArray>?,

    // Multiclass Classification Metrics
    val multiClassAccuracy: Float?,
    val multiClassBalancedAccuracy: Float?,
    @Column(columnDefinition = "float8[]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val multiClassPrecision: FloatArray?,
    @Column(columnDefinition = "float8[]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val multiClassRecall: FloatArray?,
    @Column(columnDefinition = "float8[]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val multiClassF1Score: FloatArray?,
    @Column(columnDefinition = "float8[]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val multiClassJaccard: FloatArray?,
    @Column(columnDefinition = "float8[]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val multiClassMatthewsCorrCoef: Float?,
    @Column(columnDefinition = "float8[][]")
    @Type(value = org.jaqpot.api.entity.customtypes.FloatListType::class)
    val multiClassConfusionMatrix: Array<FloatArray>?
) : BaseEntity()

