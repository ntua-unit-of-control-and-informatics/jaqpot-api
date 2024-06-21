package org.jaqpot.api.service.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.jaqpot.api.model.DatasetDto

@JsonInclude(JsonInclude.Include.NON_NULL)
class PredictionRequestDto(
    val rawModel: List<String>,
    val dataset: DatasetDto,
    val additionalInfo: AdditionalInfoDto,
    val doaMatrix: Array<Array<Float>>? = null,
)
