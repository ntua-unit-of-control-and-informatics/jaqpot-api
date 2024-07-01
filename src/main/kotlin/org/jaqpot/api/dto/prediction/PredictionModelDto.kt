package org.jaqpot.api.dto.prediction

import org.jaqpot.api.model.FeatureDto
import org.jaqpot.api.model.ModelDto

class PredictionModelDto(
    val id: Long?,
    val dependentFeatures: List<FeatureDto>,
    val independentFeatures: List<FeatureDto>,
    val type: ModelDto.Type,
    val rawModel: String,
    val legacyAdditionalInfo: Map<String, Any>? = emptyMap(),
    val legacyPredictionService: String?
)
