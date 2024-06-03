package org.jaqpot.api.service.model

import com.fasterxml.jackson.annotation.JsonInclude
import org.jaqpot.api.entity.Dataset

@JsonInclude(JsonInclude.Include.ALWAYS)
class PredictionRequestDto(
    val rawModel: List<String>,
    val dataset: Dataset,
    val additionalInfo: String? = null,
    val doaMatrix: String? = null
)
