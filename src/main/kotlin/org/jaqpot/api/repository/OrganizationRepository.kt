package org.jaqpot.api.repository

import org.jaqpot.api.entity.Organization
import org.springframework.data.repository.CrudRepository

interface OrganizationRepository : CrudRepository<Organization, Long> {
    fun findByName(name: String): Organization?
}
