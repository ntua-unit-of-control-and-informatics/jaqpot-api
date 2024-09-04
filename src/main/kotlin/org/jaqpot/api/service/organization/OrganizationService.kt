package org.jaqpot.api.service.organization

import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.jaqpot.api.OrganizationApiDelegate
import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.entity.Organization
import org.jaqpot.api.entity.OrganizationVisibility
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.mapper.toOrganizationUserDto
import org.jaqpot.api.model.OrganizationDto
import org.jaqpot.api.model.OrganizationUserDto
import org.jaqpot.api.model.PartialUpdateOrganizationRequestDto
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.jaqpot.api.service.model.ModelService
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.cache.annotation.CacheEvict
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
    private val userService: UserService,
    modelService: ModelService
) : OrganizationApiDelegate {

    override fun getAllOrganizationsForUser(): ResponseEntity<List<OrganizationDto>> {
        val userId = authenticationFacade.userId

        if (authenticationFacade.isAdmin) {
            return ResponseEntity.ok(
                organizationRepository.findAll().map { it.toDto(organizationMembers = emptyList()) })
        }

        val publicOrganizations = organizationRepository.findAllByVisibility(OrganizationVisibility.PUBLIC)
        val userOrganizations = organizationRepository.findByCreatorIdOrUserIdsContaining(userId, userId)
        val allOrganizationsForUser = publicOrganizations + userOrganizations

        return ResponseEntity.ok(allOrganizationsForUser.distinctBy { org -> org.id }
            .map { it.toDto(organizationMembers = emptyList()) })
    }

    override fun getAllOrganizationsByUser(): ResponseEntity<List<OrganizationDto>> {
        val userId = authenticationFacade.userId
        return ResponseEntity.ok(
            organizationRepository.findByCreatorIdOrUserIdsContaining(userId, userId)
                .map { it.toDto(isCreator = it.creatorId == userId, organizationMembers = emptyList()) })
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
    fun getOrganizationById(id: Long): ResponseEntity<OrganizationDto> {
        val organization = organizationRepository.findById(id)
            .orElseThrow { NotFoundException("Organization with id $id not found.") }

        val userCanEdit = authenticationFacade.userId == organization.creatorId
        val organizationMembers = getOrganizationUserDtos(organization)
        return ResponseEntity.ok(
            organization.toDto(
                userCanEdit = userCanEdit,
                organizationMembers = organizationMembers
            )
        )
    }

    private fun getOrganizationUserDtos(organization: Organization): List<OrganizationUserDto> {
        val organizationMembers = organization.organizationMembers.map {
            val userDto = userService.getUserById(it.userId).orElseThrow()

            it.toOrganizationUserDto(userDto.email!!)
        }
        return organizationMembers
    }

    @PostAuthorize("@getOrganizationAuthorizationLogic.decide(#root)")
    override fun getOrganizationByName(name: String): ResponseEntity<OrganizationDto> {
        val organization = organizationRepository.findByName(name)
            .orElseThrow { NotFoundException("Organization with name $name not found.") }

        val organizationMembers = getOrganizationUserDtos(organization)
        val userCanEdit = authenticationFacade.userId == organization.creatorId
        return ResponseEntity.ok(
            organization.toDto(
                userCanEdit = userCanEdit,
                organizationMembers = organizationMembers
            )
        )
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

        val organizationMembers = getOrganizationUserDtos(updatedOrganization)
        val userCanEdit = authenticationFacade.isAdmin || authenticationFacade.userId == updatedOrganization.creatorId
        return ResponseEntity.ok(
            updatedOrganization.toDto(
                userCanEdit = userCanEdit,
                organizationMembers = organizationMembers
            )
        )
    }

}
