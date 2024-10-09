package org.jaqpot.api.dto.prediction

import org.jaqpot.api.entity.DoaMethod
import java.io.Serializable
import java.time.OffsetDateTime

/**
 * DTO for {@link org.jaqpot.api.entity.Doa}
 */
data class PredictionDoaDto(
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
    val id: Long? = 0,
    val method: DoaMethod? = null,
    val doaData: Any? = null
) : Serializable
