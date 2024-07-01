package org.jaqpot.api.service.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.jaqpot.api.dto.prediction.PredictionModelDto
import org.jaqpot.api.model.DatasetDto

@JsonInclude(JsonInclude.Include.NON_NULL)
class PredictionRequestDto(
    val model: PredictionModelDto,
    val dataset: DatasetDto,
    val doa: Any? = null
)
