package org.jaqpot.api.service.model.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
class PredictionResponseDto(
    val predictions: List<Map<String, Any>>
)
