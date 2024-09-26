package org.jaqpot.api.service.lead

import jakarta.ws.rs.BadRequestException
import org.jaqpot.api.LeadApiDelegate
import org.jaqpot.api.entity.Lead
import org.jaqpot.api.entity.LeadStatus
import org.jaqpot.api.error.JaqpotNotFoundException
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.model.LeadDto
import org.jaqpot.api.repository.LeadRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.jaqpot.api.service.email.EmailModelHelper
import org.jaqpot.api.service.email.EmailService
import org.jaqpot.api.service.email.freemarker.FreemarkerTemplate
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@Service
class LeadService(
    private val leadRepository: LeadRepository,
    val userService: UserService,
    val emailService: EmailService,
    val authenticationFacade: AuthenticationFacade
) : LeadApiDelegate {
    @PreAuthorize("hasAuthority('admin')")
    override fun getLeadById(id: Long): ResponseEntity<LeadDto> {
        val leadDto =
            leadRepository.findById(id).orElseThrow { JaqpotNotFoundException("No lead with id $id found") }
                .toDto()
        return ResponseEntity.ok().body(leadDto)
    }

    @PreAuthorize("hasAuthority('admin')")
    override fun getAllLeads(): ResponseEntity<List<LeadDto>> {
        val leads = leadRepository.findAll().map { it.toDto() }
        return ResponseEntity.ok().body(leads)
    }

    @WithRateLimitProtectionByUser(limit = 5, intervalInSeconds = 10 * 60)
    override fun createLead(): ResponseEntity<Unit> {
        val userId = authenticationFacade.userId
        val user =
            userService.getUserById(userId).orElseThrow { JaqpotNotFoundException("No user with id $userId found") }

        if (leadRepository.findByEmail(user.email!!).isPresent) {
            throw BadRequestException("Early access request with email ${user.email} already exists.")
        }

        val lead = Lead(null, user.email, "${user.firstName} ${user.lastName}", LeadStatus.PENDING)
        val savedLead = leadRepository.save(lead)

        val model = EmailModelHelper.generateLeadRequestEmailModel(
            recipientName = user.firstName.orEmpty()
        )

        emailService.sendHTMLEmail(
            user.email,
            "Early access request received",
            FreemarkerTemplate.LEAD_REQUEST,
            model
        )

        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(savedLead.id).toUri()
        return ResponseEntity.created(location).build()
    }

    @PreAuthorize("hasAuthority('admin')")
    override fun deleteLeadById(id: Long): ResponseEntity<Unit> {
        throw NotImplementedError("Not implemented")
    }

    @PreAuthorize("hasAuthority('admin')")
    override fun updateLeadById(id: Long, leadDto: LeadDto): ResponseEntity<Unit> {
        throw NotImplementedError("Not implemented")
    }

}
