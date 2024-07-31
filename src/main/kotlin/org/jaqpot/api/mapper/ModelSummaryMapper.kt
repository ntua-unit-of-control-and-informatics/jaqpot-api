package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.ModelSummaryDto
import org.jaqpot.api.model.UserDto

fun Model.toModelSummaryDto(creatorDto: UserDto?): ModelSummaryDto {
    return ModelSummaryDto(
        id = this.id!!,
        type = this.type.toDto(),
        name = this.name,
        description = this.description,
        visibility = this.visibility.toDto(),
        creator = creatorDto,
        dependentFeaturesLength = this.dependentFeatures.size,
        independentFeaturesLength = this.independentFeatures.size,
        sharedWithOrganizations = this.sharedWithOrganizations.map { it.organization.toOrganizationSummaryDto() },
        createdAt = this.createdAt!!,
        updatedAt = this.updatedAt,
    )
}
