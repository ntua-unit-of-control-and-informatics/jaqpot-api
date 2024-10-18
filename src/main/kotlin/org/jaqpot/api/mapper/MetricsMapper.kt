package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Metrics
import org.jaqpot.api.entity.Model
import org.jaqpot.api.entity.ModelTask
import org.jaqpot.api.model.BinaryClassificationMetricsDto
import org.jaqpot.api.model.ModelMetricsDto
import org.jaqpot.api.model.MulticlassClassificationMetricsDto
import org.jaqpot.api.model.RegressionMetricsDto

fun Metrics.toDto(): ModelMetricsDto {
    when (this.model.task) {
        ModelTask.REGRESSION -> {
            return ModelMetricsDto(
                regression = RegressionMetricsDto(
                    this.r2,
                    this.mae,
                    this.rmse,
                    this.rSquaredDiffRZero,
                    this.rSquaredDiffRZeroHat,
                    this.absDiffRZeroHat,
                    this.k,
                    this.kHat
                )
            )
        }

        ModelTask.BINARY_CLASSIFICATION -> {
            return ModelMetricsDto(
                binaryClassification = BinaryClassificationMetricsDto(
                    this.accuracy,
                    this.balancedAccuracy,
                    this.precision?.toList(),
                    this.recall?.toList(),
                    this.f1Score?.toList(),
                    this.jaccard?.toList(),
                    this.matthewsCorrCoef,
                    this.confusionMatrix?.toList()?.map { it.toList() }
                )
            )
        }

        ModelTask.MULTICLASS_CLASSIFICATION -> {
            return ModelMetricsDto(
                multiclassClassification = MulticlassClassificationMetricsDto(
                    this.multiClassAccuracy,
                    this.multiClassBalancedAccuracy,
                    this.multiClassPrecision?.toList(),
                    this.multiClassRecall?.toList(),
                    this.multiClassF1Score?.toList()
                )
            )
        }
    }
}

fun ModelMetricsDto.toEntity(model: Model): Metrics {
    return Metrics(
        model = model,
        r2 = this.regression?.r2,
        mae = this.regression?.mae,
        rmse = this.regression?.rmse,
        rSquaredDiffRZero = this.regression?.rSquaredDiffRZero,
        rSquaredDiffRZeroHat = this.regression?.rSquaredDiffRZeroHat,
        absDiffRZeroHat = this.regression?.absDiffRZeroHat,
        k = this.regression?.k,
        kHat = this.regression?.kHat,
        accuracy = this.binaryClassification?.accuracy,
        balancedAccuracy = this.binaryClassification?.balancedAccuracy,
        precision = this.binaryClassification?.precision?.toFloatArray(),
        recall = this.binaryClassification?.recall?.toFloatArray(),
        f1Score = this.binaryClassification?.f1Score?.toFloatArray(),
        jaccard = this.binaryClassification?.jaccard?.toFloatArray(),
        matthewsCorrCoef = this.binaryClassification?.matthewsCorrCoef,
        confusionMatrix = this.binaryClassification?.confusionMatrix?.map { it.toFloatArray() }
            ?.toTypedArray(),
        multiClassAccuracy = this.multiclassClassification?.accuracy,
        multiClassBalancedAccuracy = this.multiclassClassification?.balancedAccuracy,
        multiClassPrecision = this.multiclassClassification?.precision?.toFloatArray(),
        multiClassRecall = this.multiclassClassification?.recall?.toFloatArray(),
        multiClassF1Score = this.multiclassClassification?.f1Score?.toFloatArray(),
        multiClassJaccard = this.multiclassClassification?.jaccard?.toFloatArray(),
        multiClassMatthewsCorrCoef = this.multiclassClassification?.matthewsCorrCoef,
        multiClassConfusionMatrix = this.multiclassClassification?.confusionMatrix?.map { it.toFloatArray() }
            ?.toTypedArray()
    )
}
