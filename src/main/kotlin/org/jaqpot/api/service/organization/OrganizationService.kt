package org.jaqpot.api.service.organization

import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.jaqpot.api.OrganizationApiDelegate
import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.entity.Organization
import org.jaqpot.api.entity.OrganizationVisibility
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.OrganizationDto
import org.jaqpot.api.model.PartialUpdateOrganizationRequestDto
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@Service
class OrganizationService(
    private val authenticationFacade: AuthenticationFacade,
    private val organizationRepository: OrganizationRepository,
) : OrganizationApiDelegate {

    override fun getAllOrganizationsForUser(): ResponseEntity<List<OrganizationDto>> {
        val userId = authenticationFacade.userId

        if (authenticationFacade.isAdmin) {
            return ResponseEntity.ok(organizationRepository.findAll().map { it.toDto() })
        }

        val publicOrganizations = getAllPublicOrganizations()
        val userOrganizations = organizationRepository.findByCreatorIdOrUserIdsContaining(userId, userId)
        val allOrganizationsForUser = publicOrganizations + userOrganizations

        return ResponseEntity.ok(allOrganizationsForUser.distinctBy { org -> org.id }.map { it.toDto() })
    }

    @Cacheable(cacheNames = [CacheKeys.ALL_PUBLIC_ORGANIZATIONS])
    fun getAllPublicOrganizations() = organizationRepository.findAllByVisibility(OrganizationVisibility.PUBLIC)

    override fun getAllOrganizationsByUser(): ResponseEntity<List<OrganizationDto>> {
        val userId = authenticationFacade.userId
        return ResponseEntity.ok(
            organizationRepository.findByCreatorIdOrUserIdsContaining(userId, userId).map { it.toDto() })
    }

    @CacheEvict(cacheNames = [CacheKeys.ALL_PUBLIC_ORGANIZATIONS, CacheKeys.USER_ORGANIZATIONS], allEntries = true)
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

    @PostAuthorize("@getOrganizationAuthorizationLogic.decide(#root)")
    override fun getOrganizationByName(name: String): ResponseEntity<OrganizationDto> {
        val organization = organizationRepository.findByName(name)
            .orElseThrow { NotFoundException("Organization with name $name not found.") }

        val userCanEdit = authenticationFacade.userId == organization.creatorId
        return ResponseEntity.ok(organization.toDto(userCanEdit))
    }

    @PreAuthorize("@partialOrganizationUpdateAuthorizationLogic.decide(#root, #id)")
    @WithRateLimitProtectionByUser(limit = 5, intervalInSeconds = 60)
    override fun partialUpdateOrganization(
        id: Long,
        partialUpdateOrganizationRequestDto: PartialUpdateOrganizationRequestDto
    ): ResponseEntity<OrganizationDto> {
        val existingOrganization = organizationRepository.findById(id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Organization with id $id not found")
        }
        partialUpdateOrganizationRequestDto.name.let { existingOrganization.name = it }
        partialUpdateOrganizationRequestDto.contactEmail.let { existingOrganization.contactEmail = it }
        partialUpdateOrganizationRequestDto.visibility.let { existingOrganization.visibility = it.toEntity() }
        partialUpdateOrganizationRequestDto.description?.let { existingOrganization.description = it }

        val updatedOrganization: Organization = try {
            organizationRepository.save(existingOrganization)
        } catch (e: DataIntegrityViolationException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "An organization with this name already exists", e)
        }

        val userCanEdit = authenticationFacade.isAdmin || authenticationFacade.userId == updatedOrganization.creatorId
        return ResponseEntity.ok(updatedOrganization.toDto(userCanEdit))
    }
}
