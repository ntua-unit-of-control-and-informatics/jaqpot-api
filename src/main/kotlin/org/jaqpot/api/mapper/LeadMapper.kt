package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Lead
import org.jaqpot.api.model.LeadDto

fun Lead.toDto(): LeadDto {
    return LeadDto(
        id = this.id,
        email = this.email,
        name = this.name,
        status = this.status.let { LeadDto.Status.valueOf(it.name) }
    )
}
