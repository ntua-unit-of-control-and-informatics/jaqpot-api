package org.jaqpot.api.service.organization

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import jakarta.ws.rs.BadRequestException
import org.jaqpot.api.NotFoundException
import org.jaqpot.api.config.JaqpotConfig
import org.jaqpot.api.entity.Organization
import org.jaqpot.api.entity.OrganizationInvitation
import org.jaqpot.api.entity.OrganizationInvitationStatus
import org.jaqpot.api.mapper.toDto
import org.jaqpot.api.model.OrganizationInvitationDto
import org.jaqpot.api.model.UserDto
import org.jaqpot.api.repository.OrganizationInvitationRepository
import org.jaqpot.api.repository.OrganizationRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.jaqpot.api.service.email.EmailService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class OrganizationInvitationServiceTest {

    @MockK
    lateinit var organizationRepository: OrganizationRepository

    @MockK
    lateinit var organizationInvitationRepository: OrganizationInvitationRepository

    @MockK
    lateinit var emailService: EmailService

    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var jaqpotConfig: JaqpotConfig

    @MockK
    lateinit var authenticationFacade: AuthenticationFacade

    @InjectMockKs
    private lateinit var organizationInvitationService: OrganizationInvitationService

    val date = OffsetDateTime.of(
        2012,
        10,
        10,
        10,
        10,
        10,
        10,
        ZoneOffset.UTC
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        organizationInvitationService = OrganizationInvitationService(
            organizationRepository,
            organizationInvitationRepository,
            emailService,
            userService,
            jaqpotConfig,
            authenticationFacade
        )

        mockkStatic(OffsetDateTime::class)



        every {
            OffsetDateTime.now()
        } returns date
    }

    @Test
    fun updateInvitation_throws404IfOrgNotFound() {
        val organizationInvitationDto = mockk<OrganizationInvitationDto>()

        every { organizationRepository.findByName(any()) } returns Optional.empty()

        assertThrows<NotFoundException> {
            organizationInvitationService.updateInvitation(
                "orgName",
                UUID.randomUUID(),
                organizationInvitationDto
            )
        }
    }

    @Test
    fun updateInvitation_throws404IfInvitationNotFound() {
        val organizationInvitationDto = mockk<OrganizationInvitationDto>()
        val organization = mockk<Organization>()

        every { organizationRepository.findByName(any()) } returns Optional.of(organization)
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.empty()

        assertThrows<NotFoundException> {
            organizationInvitationService.updateInvitation(
                "orgName",
                UUID.randomUUID(),
                organizationInvitationDto
            )
        }
    }

    @ParameterizedTest
    @EnumSource(names = ["ACCEPTED", "REJECTED"])
    fun updateInvitation_throwsIfStatusIsAlreadyFinal(organizationInvitationStatus: OrganizationInvitationStatus) {
        val organizationInvitationDto = mockk<OrganizationInvitationDto>()
        val organization = mockk<Organization>()
        val invitation = mockk<OrganizationInvitation>()

        every { invitation.status } returns organizationInvitationStatus

        every { organizationRepository.findByName(any()) } returns Optional.of(organization)
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.of(
            invitation
        )

        val ex = assertThrows<BadRequestException> {
            organizationInvitationService.updateInvitation(
                "orgName",
                UUID.randomUUID(),
                organizationInvitationDto
            )
        }

        assertEquals("This invitation has already status $organizationInvitationStatus", ex.message)
    }

    @Test
    fun updateInvitation_throwsIfInvitationExpired() {
        val organizationInvitationDto = mockk<OrganizationInvitationDto>()
        val organization = mockk<Organization>()
        val invitation = mockk<OrganizationInvitation>()

        every { invitation.status } returns OrganizationInvitationStatus.PENDING
        every { invitation.expirationDate } returns OffsetDateTime.now().minusDays(1L)

        every { organizationRepository.findByName(any()) } returns Optional.of(organization)
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.of(
            invitation
        )

        val ex = assertThrows<BadRequestException> {
            organizationInvitationService.updateInvitation(
                "orgName",
                UUID.randomUUID(),
                organizationInvitationDto
            )
        }

        assertEquals(
            "This invitation has expired. Please ask the organization admin to generate a new invitation",
            ex.message
        )
    }

    @Test
    fun updateInvitation_throwsIfUserNotFound() {
        val organizationInvitationDto = mockk<OrganizationInvitationDto>()
        val organization = mockk<Organization>()
        val invitation = mockk<OrganizationInvitation>()

        every { invitation.status } returns OrganizationInvitationStatus.PENDING
        every { invitation.expirationDate } returns OffsetDateTime.now().plusDays(1L)
        every { invitation.userEmail } returns "email@email.com"

        every { organizationRepository.findByName(any()) } returns Optional.of(organization)
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.of(
            invitation
        )
        every { userService.getUserByEmail(any()) } returns Optional.empty()

        val ex = assertThrows<BadRequestException> {
            organizationInvitationService.updateInvitation(
                "orgName",
                UUID.randomUUID(),
                organizationInvitationDto
            )
        }

        assertEquals(
            "It appears that you are currently logged in with an email address that does not match the one associated with this invitation. Please log in or sign up with the email address that received the invitation to proceed.",
            ex.message
        )
    }

    @Test
    fun updateInvitation_throwsIfUserFoundIsDifferentThanAuthenticatedUser() {
        val organizationInvitationDto = mockk<OrganizationInvitationDto>()
        val organization = mockk<Organization>()
        val invitation = mockk<OrganizationInvitation>()
        val user = mockk<UserDto>()
        val userId = "userId"

        every { invitation.status } returns OrganizationInvitationStatus.PENDING
        every { invitation.expirationDate } returns OffsetDateTime.now().plusDays(1L)
        every { invitation.userEmail } returns "email@email.com"

        every { organizationRepository.findByName(any()) } returns Optional.of(organization)
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.of(
            invitation
        )
        every { userService.getUserByEmail(any()) } returns Optional.of(user)

        every { user.id } returns userId
        every { authenticationFacade.userId } returns "different-user-id"

        val ex = assertThrows<BadRequestException> {
            organizationInvitationService.updateInvitation(
                "orgName",
                UUID.randomUUID(),
                organizationInvitationDto
            )
        }

        assertEquals(
            "It appears that you are currently logged in with an email address that does not match the one associated with this invitation. Please log in or sign up with the email address that received the invitation to proceed.",
            ex.message
        )
    }

    @Test
    fun updateInvitation_throwsIfUserEmailNotVerified() {
        val organizationInvitationDto = mockk<OrganizationInvitationDto>()
        val organization = mockk<Organization>()
        val invitation = mockk<OrganizationInvitation>()
        val user = mockk<UserDto>()
        val userId = "userId"

        every { invitation.status } returns OrganizationInvitationStatus.PENDING
        every { invitation.expirationDate } returns OffsetDateTime.now().plusDays(1L)
        every { invitation.userEmail } returns "email@email.com"

        every { organizationRepository.findByName(any()) } returns Optional.of(organization)
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.of(
            invitation
        )
        every { user.id } returns userId
        every { user.emailVerified } returns false
        every { userService.getUserByEmail(any()) } returns Optional.of(user)
        every { authenticationFacade.userId } returns userId

        val ex = assertThrows<BadRequestException> {
            organizationInvitationService.updateInvitation(
                "orgName",
                UUID.randomUUID(),
                organizationInvitationDto
            )
        }

        assertEquals(
            "Your email needs to be verified before you can respond to the invitation and " +
                    "potentially be added as a member of the organization.",
            ex.message,
        )
    }

    @Test
    fun updateInvitation_ifAcceptedInvitationIsGoodSavesTheUserInTheOrganization() {
        val organizationInvitationDto = mockk<OrganizationInvitationDto>()
        val organization = mockk<Organization>()
        val invitation = OrganizationInvitation(
            null,
            null,
            "email@email.com",
            organization,
            OrganizationInvitationStatus.PENDING,
            OffsetDateTime.now().plusDays(1L)
        )
        invitation.status = OrganizationInvitationStatus.PENDING
        val user = mockk<UserDto>()
        val userId = "userId"

        mockkStatic(OrganizationInvitation::toDto)
        every { any<OrganizationInvitation>().toDto() } returns organizationInvitationDto

        every { organization.organizationMembers } returns mutableListOf()

        every { organizationRepository.findByName(any()) } returns Optional.of(organization)
        every { organizationRepository.save(any()) } returns organization
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.of(
            invitation
        )
        every { organizationInvitationRepository.save(any()) } returns invitation
        every { userService.getUserByEmail(any()) } returns Optional.of(user)

        every { user.id } returns userId
        every { authenticationFacade.userId } returns userId
        every { user.emailVerified } returns true
        every { organizationInvitationDto.status } returns OrganizationInvitationDto.Status.ACCEPTED

        organizationInvitationService.updateInvitation(
            "orgName",
            UUID.randomUUID(),
            organizationInvitationDto
        )

        val invitationSlot = slot<OrganizationInvitation>()

        verify {
            organizationInvitationRepository.save(capture(invitationSlot))
        }

        assertEquals(userId, invitationSlot.captured.userId)
        assertEquals(organizationInvitationDto.status, invitationSlot.captured.status.toDto())

        verify {
            organizationRepository.save(any())
        }
    }

    @Test
    fun updateInvitation_ifRejectedInvitationDoesNotSaveTheUserInTheOrg() {
        val organizationInvitationDto = mockk<OrganizationInvitationDto>()
        val organization = mockk<Organization>()
        val invitation = OrganizationInvitation(
            null,
            null,
            "email@email.com",
            organization,
            OrganizationInvitationStatus.PENDING,
            OffsetDateTime.now().plusDays(1L)
        )
        invitation.status = OrganizationInvitationStatus.PENDING
        val user = mockk<UserDto>()
        val userId = "userId"

        mockkStatic(OrganizationInvitation::toDto)
        every { any<OrganizationInvitation>().toDto() } returns organizationInvitationDto

        every { organization.organizationMembers } returns mutableListOf()

        every { organizationRepository.findByName(any()) } returns Optional.of(organization)
        every { organizationRepository.save(any()) } returns organization
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.of(
            invitation
        )
        every { organizationInvitationRepository.save(any()) } returns invitation
        every { userService.getUserByEmail(any()) } returns Optional.of(user)

        every { user.id } returns userId
        every { authenticationFacade.userId } returns userId
        every { user.emailVerified } returns true
        every { organizationInvitationDto.status } returns OrganizationInvitationDto.Status.REJECTED

        organizationInvitationService.updateInvitation(
            "orgName",
            UUID.randomUUID(),
            organizationInvitationDto
        )

        val invitationSlot = slot<OrganizationInvitation>()

        verify {
            organizationInvitationRepository.save(capture(invitationSlot))
        }

        assertEquals(userId, invitationSlot.captured.userId)
        assertEquals(organizationInvitationDto.status, invitationSlot.captured.status.toDto())

        verify(exactly = 0) {
            organizationRepository.save(any())
        }
    }

    @Test
    fun resendInvitation_throws404IfOrgNotFound() {
        every { organizationRepository.findById(any()) } returns Optional.empty()

        assertThrows<NotFoundException> {
            organizationInvitationService.resendInvitation(1L, UUID.randomUUID().toString())
        }
    }

    @Test
    fun resendInvitation_throws404IfInvitationNotFound() {
        val organization = mockk<Organization>()

        every { organizationRepository.findById(any()) } returns Optional.of(organization)
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.empty()

        assertThrows<NotFoundException> {
            organizationInvitationService.resendInvitation(1L, UUID.randomUUID().toString())
        }
    }

    @Test
    fun resendInvitation_throwsIfStatusIsAlreadyFinal() {
        val organization = mockk<Organization>()
        val invitation = mockk<OrganizationInvitation>()

        every { invitation.status } returns OrganizationInvitationStatus.ACCEPTED

        every { organizationRepository.findById(any()) } returns Optional.of(organization)
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.of(
            invitation
        )

        val ex = assertThrows<BadRequestException> {
            organizationInvitationService.resendInvitation(1L, UUID.randomUUID().toString())
        }

        assertEquals("This invitation has already status ${invitation.status}", ex.message)
    }

    @Test
    fun resendInvitation_updatesExpirationDateAndSendsEmail() {
        val organization = mockk<Organization>()
        every { organization.name } returns "orgName"
        val invitation = mockk<OrganizationInvitation>(relaxed = true)
        val user = mockk<UserDto>()
        every { user.name } returns "userName"

        every { emailService.sendHTMLEmail(any(), any(), any(), any()) } just Runs

        every { jaqpotConfig.frontendUrl } returns "http://localhost:8080"

        every { organizationRepository.findById(any()) } returns Optional.of(organization)
        every { organizationInvitationRepository.findByIdAndOrganization(any(), organization) } returns Optional.of(
            invitation
        )
        every { organizationInvitationRepository.save(any()) } returns invitation
        every { userService.getUserByEmail(any()) } returns Optional.of(user)
        every { invitation.userEmail } returns "email@example.com"

        organizationInvitationService.resendInvitation(1L, UUID.randomUUID().toString())

        verify { invitation.expirationDate = date.plusWeeks(1) }
        verify { organizationInvitationRepository.save(invitation) }
        verify { emailService.sendHTMLEmail(any(), any(), any(), any()) }
    }
}
