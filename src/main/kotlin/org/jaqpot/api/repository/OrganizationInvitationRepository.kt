package org.jaqpot.api.repository

import org.jaqpot.api.entity.Organization
import org.jaqpot.api.entity.OrganizationInvitation
import org.springframework.data.repository.CrudRepository
import java.util.*

interface OrganizationInvitationRepository : CrudRepository<OrganizationInvitation, UUID> {
    fun findByIdAndOrganization(id: UUID, organization: Organization): Optional<OrganizationInvitation>
}
