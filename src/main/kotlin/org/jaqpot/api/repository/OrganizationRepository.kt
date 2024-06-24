package org.jaqpot.api.repository

import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.entity.Organization
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.CrudRepository
import java.util.*

interface OrganizationRepository : CrudRepository<Organization, Long> {
    fun findByName(name: String): Optional<Organization>

    @Cacheable(CacheKeys.USER_ORGANIZATIONS, keyGenerator = "organizationsByUserKeyGenerator")
    fun findByCreatorIdOrUserIdsContaining(creatorId: String, userId: String): List<Organization>
}
