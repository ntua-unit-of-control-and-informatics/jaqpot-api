package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.model.OrganizationSummaryDto

fun Organization.toOrganizationSummaryDto(
): OrganizationSummaryDto {
    return OrganizationSummaryDto(
        id = this.id!!,
        name = this.name,
    )
}
