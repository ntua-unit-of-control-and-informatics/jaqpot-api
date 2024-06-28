package org.jaqpot.api.service.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.ModelDto

@JsonInclude(JsonInclude.Include.NON_NULL)
class PredictionRequestDto(
    val model: ModelDto,
    val dataset: DatasetDto,
    val additionalInfo: Map<String, Any>? = emptyMap(),
    val rawModel: String,
    val doa: Any? = null
)
