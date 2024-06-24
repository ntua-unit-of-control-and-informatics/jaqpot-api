package org.jaqpot.api.repository

import org.jaqpot.api.cache.USER_ORGANIZATIONS_CACHE_KEY
import org.jaqpot.api.entity.Organization
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.CrudRepository
import java.util.*

interface OrganizationRepository : CrudRepository<Organization, Long> {
    fun findByName(name: String): Optional<Organization>

    @Cacheable(USER_ORGANIZATIONS_CACHE_KEY, keyGenerator = "organizationsByUserKeyGenerator")
    fun findByCreatorIdOrUserIdsContaining(creatorId: String, userId: String): List<Organization>
}
