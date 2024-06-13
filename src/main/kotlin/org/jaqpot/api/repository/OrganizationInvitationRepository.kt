package org.jaqpot.api.repository

import org.jaqpot.api.entity.OrganizationInvitation
import org.springframework.data.repository.CrudRepository

interface OrganizationInvitationRepository : CrudRepository<OrganizationInvitation, Long>
