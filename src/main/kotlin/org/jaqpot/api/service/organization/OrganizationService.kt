package org.jaqpot.api.service.organization

import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.jaqpot.api.OrganizationApiDelegate
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.OrganizationDto
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@Service
class OrganizationService(
    private val authenticationFacade: AuthenticationFacade,
    private val organizationRepository: OrganizationRepository,
) : OrganizationApiDelegate {

    @Cacheable(cacheNames = ["allOrganizations"])
    override fun getAllOrganizations(): ResponseEntity<List<OrganizationDto>> {
        return ResponseEntity.ok(organizationRepository.findAll().map { it.toDto() })
    }

    // TODO add cache
    override fun getAllOrganizationsByUser(): ResponseEntity<List<OrganizationDto>> {
        val userId = authenticationFacade.userId
        return ResponseEntity.ok(
            organizationRepository.findByCreatorIdOrUserIdsContaining(userId, userId).map { it.toDto() })
    }

    @CacheEvict(cacheNames = ["allOrganizations"], allEntries = true)
    @WithRateLimitProtectionByUser(limit = 2, intervalInSeconds = 60)
    override fun createOrganization(organizationDto: OrganizationDto): ResponseEntity<Unit> {
        if (organizationDto.id != null) {
            throw IllegalStateException("ID should not be provided for resource creation.")
        }

        if (organizationRepository.findByName(organizationDto.name).isPresent) {
            throw BadRequestException("Organization with name ${organizationDto.name} already exists.")
        }

        val creatorId = authenticationFacade.userId
        val organization = organizationRepository.save(organizationDto.toEntity(creatorId))
        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{name}")
            .buildAndExpand(organization.name).toUri()
        return ResponseEntity.created(location).build()
    }

    override fun getOrganizationByName(name: String): ResponseEntity<OrganizationDto> {
        val organization = organizationRepository.findByName(name)
            .orElseThrow { NotFoundException("Organization with name $name not found.") }

        val userCanEdit = authenticationFacade.userId == organization.creatorId
        return ResponseEntity.ok(organization.toDto(userCanEdit))
    }

    // TODO implement
    @WithRateLimitProtectionByUser(limit = 5, intervalInSeconds = 60)
    override fun updateOrganization(id: Long, organizationDto: OrganizationDto): ResponseEntity<OrganizationDto> {
        return super.updateOrganization(id, organizationDto)
    }
}
