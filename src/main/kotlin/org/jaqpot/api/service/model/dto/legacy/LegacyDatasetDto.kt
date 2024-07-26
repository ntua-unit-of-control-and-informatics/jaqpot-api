package org.jaqpot.api.service.model.dto.legacy

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class LegacyDatasetDto(
    val dataEntry: LegacyDataEntryDto,
    val features: List<Map<String, Any>>
)

class LegacyDataEntryDto(
    val values: Map<Int, Any>
)
