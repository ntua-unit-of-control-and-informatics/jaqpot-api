package org.jaqpot.api.dto.prediction

import org.jaqpot.api.model.FeatureDto
import org.jaqpot.api.model.ModelTaskDto
import org.jaqpot.api.model.ModelTypeDto
import org.jaqpot.api.model.TransformerDto

class PredictionModelDto(
    val id: Long?,
    val dependentFeatures: List<FeatureDto>,
    val independentFeatures: List<FeatureDto>,
    val type: ModelTypeDto,
    val rawModel: String,
    val doas: List<PredictionDoaDto>,
    val selectedFeatures: List<String>,
    val task: ModelTaskDto,
    val featurizers: List<TransformerDto>,
    val preprocessros: List<TransformerDto>,
    val torchConfig: Map<String, Any>? = emptyMap(),
    val extraConfig: Map<String, Any>? = emptyMap(),
    val legacyAdditionalInfo: Map<String, Any>? = emptyMap(),
    val legacyPredictionService: String?
)
