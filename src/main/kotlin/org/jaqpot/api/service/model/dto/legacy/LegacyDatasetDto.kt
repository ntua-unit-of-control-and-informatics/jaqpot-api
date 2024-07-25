package org.jaqpot.api.service.model.dto.legacy

import com.fasterxml.jackson.annotation.JsonInclude
import org.jaqpot.api.model.FeatureDto

@JsonInclude(JsonInclude.Include.NON_NULL)
class LegacyDatasetDto(
    val dataEntry: LegacyDataEntryDto,
    val features: List<FeatureDto>
)

class LegacyDataEntryDto(
    val values: List<Any>
)
