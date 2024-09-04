package org.jaqpot.api.repository

import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.entity.Organization
import org.jaqpot.api.entity.OrganizationVisibility
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface OrganizationRepository : CrudRepository<Organization, Long> {
    @Cacheable(cacheNames = [CacheKeys.ALL_PUBLIC_ORGANIZATIONS])
    fun findAllByVisibility(visibility: OrganizationVisibility): List<Organization>
    fun findByName(name: String): Optional<Organization>

    @Cacheable(CacheKeys.USER_ORGANIZATIONS, keyGenerator = "organizationsByUserKeyGenerator")
    @Query(
        """
        SELECT o FROM Organization o
        LEFT JOIN o.organizationMembers u
        WHERE u.userId = :userId OR o.creatorId = :creatorId
        """
    )
    fun findByCreatorIdOrUserIdsContaining(creatorId: String, userId: String): List<Organization>

}
