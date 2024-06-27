package org.jaqpot.api.service.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.jaqpot.api.model.DatasetDto

@JsonInclude(JsonInclude.Include.ALWAYS)
class PredictionRequestDto(
    val rawModel: String,
    val dataset: DatasetDto,
    val additionalInfo: String? = null,
    val doaMatrix: String? = null
)
