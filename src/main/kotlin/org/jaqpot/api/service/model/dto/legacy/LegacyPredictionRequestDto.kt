package org.jaqpot.api.service.model.dto.legacy

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class LegacyPredictionRequestDto(
    val rawModel: String,
    val dataset: LegacyDatasetDto,
    val additionalInfo: Map<String, Any>? = emptyMap(),
    val doa: Any? = null
)
