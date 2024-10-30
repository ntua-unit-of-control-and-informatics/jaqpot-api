package org.jaqpot.api.service.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto

@JsonInclude(JsonInclude.Include.NON_NULL)
class PredictionRequestDto(
    val model: PredictionModelDto,
    val dataset: DatasetDto,
    val extraConfig: Map<String, Any>? = mapOf(),
    val doa: Any? = null
)
