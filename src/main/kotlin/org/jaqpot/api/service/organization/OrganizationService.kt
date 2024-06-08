package org.jaqpot.api.service.organization

import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.jaqpot.api.OrganizationApiDelegate
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.OrganizationDto
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@Service
class OrganizationService(
    private val authenticationFacade: AuthenticationFacade,
    private val organizationRepository: OrganizationRepository,
    private val userService: UserService,
) : OrganizationApiDelegate {
    override fun createOrganization(organizationDto: OrganizationDto): ResponseEntity<Unit> {
        if (organizationDto.id != null) {
            throw IllegalStateException("ID should not be provided for resource creation.")
        }

        if (organizationRepository.findByName(organizationDto.name) != null) {
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
            ?: throw NotFoundException("Organization with name $name not found.")

        val creatorDto = userService.getUserById(organization.creatorId)
        return ResponseEntity.ok(organization.toDto(creatorDto))
    }
}
