package org.jaqpot.api.service.organization

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import jakarta.ws.rs.BadRequestException
import org.jaqpot.api.NotFoundException
import org.jaqpot.api.OrganizationInvitationApiDelegate
import org.jaqpot.api.cache.CacheKeys
import org.jaqpot.api.config.JaqpotConfig
import org.jaqpot.api.entity.*
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.mapper.toEntity
import org.jaqpot.api.model.CreateInvitationsRequestDto
import org.jaqpot.api.model.OrganizationInvitationDto
import org.jaqpot.api.model.UserDto
import org.jaqpot.api.repository.OrganizationInvitationRepository
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.jaqpot.api.service.email.EmailModelHelper
import org.jaqpot.api.service.email.EmailService
import org.jaqpot.api.service.email.freemarker.FreemarkerTemplate
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*


@Service
class OrganizationInvitationService(
    private val organizationRepository: OrganizationRepository,
    private val organizationInvitationRepository: OrganizationInvitationRepository,
    private val emailService: EmailService,
    private val userService: UserService,
    private val jaqpotConfig: JaqpotConfig,
    private val authenticationFacade: AuthenticationFacade
) : OrganizationInvitationApiDelegate {
    companion object {
        const val ORGANIZATION_INVITATION_EMAIL_SUBJECT = "Jaqpot organization invitation"
        val logger = KotlinLogging.logger {}
    }

    @PreAuthorize("@organizationInviteAuthorizationLogic.decide(#root, #orgName)")
    override fun getAllInvitations(orgName: String): ResponseEntity<List<OrganizationInvitationDto>> {
        val organization = organizationRepository.findByName(orgName)
            .orElseThrow { NotFoundException("Organization $orgName not found") }

        return ResponseEntity.ok(organization.organizationInvitations.map { it.toDto() })
    }

    override fun getInvitation(name: String, uuid: UUID): ResponseEntity<OrganizationInvitationDto> {
        val organization = organizationRepository.findByName(name)
            .orElseThrow { NotFoundException("Organization $name not found") }

        val invitation = organizationInvitationRepository.findByIdAndOrganization(uuid, organization)
            .orElseThrow { NotFoundException("Invitation with id $uuid not found") }

        return ResponseEntity.ok(invitation.toDto())
    }

    @Transactional
    @CacheEvict(cacheNames = [CacheKeys.USER_ORGANIZATIONS], allEntries = true)
    override fun updateInvitation(
        name: String,
        uuid: UUID,
        organizationInvitationDto: OrganizationInvitationDto
    ): ResponseEntity<OrganizationInvitationDto> {
        val organization = organizationRepository.findByName(name)
            .orElseThrow { NotFoundException("Organization $name not found") }

        val invitation = organizationInvitationRepository.findByIdAndOrganization(uuid, organization)
            .orElseThrow { NotFoundException("Invitation with id $uuid not found") }

        if (invitation.status == OrganizationInvitationStatus.ACCEPTED || invitation.status == OrganizationInvitationStatus.REJECTED) {
            throw BadRequestException("This invitation has already status ${invitation.status}")
        }

        if (invitation.expirationDate.isBefore(OffsetDateTime.now())) {
            throw BadRequestException("This invitation has expired. Please ask the organization admin to generate a new invitation")
        }

        val user = userService.getUserByEmail(invitation.userEmail)
            .orElseThrow {
                BadRequestException(
                    "It appears that you are currently logged in with an email address that does " +
                            "not match the one associated with this invitation. " +
                            "Please log in or sign up with the email address that received the invitation to proceed."
                )
            }

        if (user.id != authenticationFacade.userId) {
            throw BadRequestException(
                "It appears that you are currently logged in with an email address that does " +
                        "not match the one associated with this invitation. " +
                        "Please log in or sign up with the email address that received the invitation to proceed."
            )
        }

        if (user.emailVerified == null || user.emailVerified == false) {
            throw BadRequestException(
                "Your email needs to be verified before you can respond to the invitation and " +
                        "potentially be added as a member of the organization."
            )
        }

        invitation.userId = authenticationFacade.userId
        invitation.status = organizationInvitationDto.status.toEntity()

        val updatedInvitation = organizationInvitationRepository.save(invitation)

        if (updatedInvitation.status == OrganizationInvitationStatus.ACCEPTED) {
            organization.organizationMembers.add(
                OrganizationUserAssociation(
                    null,
                    authenticationFacade.userId,
                    organization,
                    OrganizationUserAssociationType.MEMBER
                )
            )

            organizationRepository.save(organization)
        }

        return ResponseEntity.ok(updatedInvitation.toDto())
    }

    @WithRateLimitProtectionByUser(limit = 5, intervalInSeconds = 60 * 10)
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
                    user.map { it.id }.orElse(null),
                    email,
                    organization,
                    OrganizationInvitationStatus.PENDING,
                    OffsetDateTime.now().plusWeeks(1)
                )
            )

            organizationInvitation to user
        }?.toMap() ?: emptyMap()

        userOrganizationInvitationDictionary.forEach { (organizationInvitation, user) ->
            try {
                sendInvitationEmail(organization, organizationInvitation, orgName, user)
            } catch (e: Exception) {
                logger.error(e) { "Failed to send email for invitation ${organizationInvitation.id}" }
            }
        }

        return ResponseEntity.ok(Unit)
    }

    private fun sendInvitationEmail(
        organization: Organization,
        organizationInvitation: OrganizationInvitation,
        orgName: String,
        user: Optional<UserDto>
    ) {
        val invitationActionUrl = generateInvitationActionUrl(
            organization,
            organizationInvitation,
            jaqpotConfig
        )
        val model = EmailModelHelper.generateOrganizationInvitationEmailModel(
            invitationActionUrl,
            orgName,
            user.map { it.firstName.orEmpty() }.orElse("")
        )
        emailService.sendHTMLEmail(
            organizationInvitation.userEmail,
            ORGANIZATION_INVITATION_EMAIL_SUBJECT,
            FreemarkerTemplate.ORGANIZATION_INVITATION,
            model
        )

    }

    @WithRateLimitProtectionByUser(limit = 10, intervalInSeconds = 60 * 10)
    @PreAuthorize("@resendInvitationAuthorizationLogic.decide(#root, #orgId)")
    override fun resendInvitation(orgId: Long, id: String): ResponseEntity<Unit> {
        val organization = organizationRepository.findById(orgId)
            .orElseThrow { NotFoundException("Organization with id $orgId not found") }

        val invitation = organizationInvitationRepository.findByIdAndOrganization(UUID.fromString(id), organization)
            .orElseThrow { NotFoundException("Invitation with id $id not found") }

        if (invitation.status == OrganizationInvitationStatus.ACCEPTED || invitation.status == OrganizationInvitationStatus.REJECTED) {
            throw BadRequestException("This invitation has already status ${invitation.status}")
        }

        // refresh expiration date for the invitation to 1 week from now
        invitation.expirationDate = OffsetDateTime.now().plusWeeks(1)

        organizationInvitationRepository.save(invitation)

        sendInvitationEmail(
            organization,
            invitation,
            organization.name,
            userService.getUserByEmail(invitation.userEmail)
        )

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
