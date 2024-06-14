package org.jaqpot.api.service.organization

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.NotFoundException
import org.jaqpot.api.OrganizationInvitationApiDelegate
import org.jaqpot.api.config.JaqpotConfig
import org.jaqpot.api.entity.Organization
import org.jaqpot.api.entity.OrganizationInvitation
import org.jaqpot.api.entity.OrganizationInvitationStatus
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.model.CreateInvitationsRequestDto
import org.jaqpot.api.model.OrganizationInvitationDto
import org.jaqpot.api.repository.OrganizationInvitationRepository
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.UserService
import org.jaqpot.api.service.email.EmailModelHelper
import org.jaqpot.api.service.email.EmailService
import org.jaqpot.api.service.email.freemarker.FreemarkerTemplate
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class OrganizationInvitationService(
    private val organizationRepository: OrganizationRepository,
    private val organizationInvitationRepository: OrganizationInvitationRepository,
    private val emailService: EmailService,
    private val userService: UserService,
    private val jaqpotConfig: JaqpotConfig
) : OrganizationInvitationApiDelegate {
    companion object {
        const val ORGANIZATION_INVITATION_EMAIL_SUBJECT = "Jaqpot organization invitation"
    }

    override fun getInvitation(name: String, uuid: UUID): ResponseEntity<OrganizationInvitationDto> {
        val organization = organizationRepository.findByName(name)
            .orElseThrow { NotFoundException("Organization $name not found") }

        val invitation = organizationInvitationRepository.findByIdAndOrganization(uuid, organization)
            .orElseThrow { NotFoundException("Invitation with id $uuid not found") }

        return ResponseEntity.ok(invitation.toDto())
    }

    @PreAuthorize("@organizationInviteAuthorizationLogic.decide(#root, #orgName)")
    override fun createInvitations(
        orgName: String,
        createInvitationsRequestDto: CreateInvitationsRequestDto
    ): ResponseEntity<Unit> {
        val organization = organizationRepository.findByName(orgName)
            .orElseThrow { NotFoundException("Organization $orgName not found") }

        val userOrganizationInvitationDictionary = createInvitationsRequestDto.emails?.map { email ->
            val user = userService.getUserByEmail(email)

            val organizationInvitation = organizationInvitationRepository.save(
                OrganizationInvitation(
                    null,
                    user.map { it -> it.id }.orElse(null),
                    email,
                    organization,
                    OrganizationInvitationStatus.PENDING,
                    LocalDateTime.now().plusWeeks(1)
                )
            )

            organizationInvitation to user
        }?.toMap() ?: emptyMap()

        userOrganizationInvitationDictionary.forEach { (organizationInvitation, user) ->
            val invitationActionUrl = generateInvitationActionUrl(
                organization,
                organizationInvitation,
                jaqpotConfig
            )
            val model = EmailModelHelper.generateOrganizationInvitationEmailModel(
                invitationActionUrl,
                orgName,
                user.map { it -> it.name.orEmpty() }.orElse("")
            )
            try {
                emailService.sendHTMLEmail(
                    organizationInvitation.userEmail,
                    ORGANIZATION_INVITATION_EMAIL_SUBJECT,
                    FreemarkerTemplate.DEFAULT,
                    model
                )
            } catch (e: Exception) {
                logger.error { "Failed to send email for invitation ${organizationInvitation.id}" }
            }
        }

        return ResponseEntity.ok(Unit)
    }

    private fun generateInvitationActionUrl(
        organization: Organization,
        organizationInvitation: OrganizationInvitation,
        jaqpotConfig: JaqpotConfig
    ): String {
        return "${jaqpotConfig.frontendUrl}/dashboard/organizations/${organization.name}/invitations/${organizationInvitation.id}"
    }
}
