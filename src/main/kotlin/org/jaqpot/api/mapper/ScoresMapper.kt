package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Model
import org.jaqpot.api.entity.ScoreType
import org.jaqpot.api.entity.Scores
import org.jaqpot.api.model.BinaryClassificationScoresDto
import org.jaqpot.api.model.MulticlassClassificationScoresDto
import org.jaqpot.api.model.RegressionScoresDto
import org.jaqpot.api.model.ScoresDto


fun Scores.toDto(): ScoresDto {
    return ScoresDto(
        regression = RegressionScoresDto(
            yName = this.yName,
            r2 = this.r2,
            mae = this.mae,
            rmse = this.rmse,
            rSquaredDiffRZero = this.rSquaredDiffRZero,
            rSquaredDiffRZeroHat = this.rSquaredDiffRZeroHat,
            absDiffRZeroHat = this.absDiffRZeroHat,
            k = this.k,
            kHat = this.kHat
        ),
        binaryClassification = BinaryClassificationScoresDto(
            yName = this.yName,
            accuracy = this.accuracy,
            balancedAccuracy = this.balancedAccuracy,
            precision = this.precision?.toList(),
            recall = this.recall?.toList(),
            f1Score = this.f1Score?.toList(),
            jaccard = this.jaccard?.toList(),
            matthewsCorrCoef = this.matthewsCorrCoef,
            confusionMatrix = this.confusionMatrix?.toList()?.map { it.toList() }
        ),
        multiclassClassification = MulticlassClassificationScoresDto(
            yName = this.yName,
            accuracy = this.multiClassAccuracy,
            balancedAccuracy = this.multiClassBalancedAccuracy,
            precision = this.multiClassPrecision?.toList(),
            recall = this.multiClassRecall?.toList(),
            f1Score = this.multiClassF1Score?.toList(),
            jaccard = this.multiClassJaccard?.toList(),
            matthewsCorrCoef = this.multiClassMatthewsCorrCoef,
            confusionMatrix = this.multiClassConfusionMatrix?.toList()?.map { it.toList().map { it1 -> it1.toList() } },
        )
    )
}

fun ScoresDto.toEntity(model: Model, scoreType: ScoreType): Scores {
    return Scores(
        model = model,
        scoreType = scoreType,
        yName = this.regression?.yName ?: this.binaryClassification?.yName
        ?: this.multiclassClassification?.yName ?: "",
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
        multiClassConfusionMatrix = this.multiclassClassification?.confusionMatrix?.map {
            it.map { it1 -> it1.toFloatArray() }.toTypedArray()
        }
            ?.toTypedArray()
    )
}
