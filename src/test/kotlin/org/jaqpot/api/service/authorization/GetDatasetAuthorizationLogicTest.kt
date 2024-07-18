package org.jaqpot.api.service.authorization

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations

class GetDatasetAuthorizationLogicTest {

    private lateinit var authenticationFacade: AuthenticationFacade
    private lateinit var getDatasetAuthorizationLogic: GetDatasetAuthorizationLogic
    private lateinit var operations: MethodSecurityExpressionOperations

    @BeforeEach
    fun setUp() {
        authenticationFacade = mockk()
        getDatasetAuthorizationLogic = GetDatasetAuthorizationLogic(authenticationFacade)
        operations = mockk()

        every { authenticationFacade.isAdmin } returns false
        every { operations.returnObject } returns ResponseEntity.ok(mockk<DatasetDto>())
    }

    @Test
    fun `should allow access for admin user`() {
        every { authenticationFacade.isAdmin } returns true

        assertTrue(getDatasetAuthorizationLogic.decide(operations))
    }

    @Test
    fun `should allow access for dataset owner`() {
        val userId = "user123"
        val datasetDto = mockk<DatasetDto>()
        every { datasetDto.userId } returns userId
        every { authenticationFacade.userId } returns userId
        every { operations.returnObject } returns ResponseEntity.ok(datasetDto)

        assertTrue(getDatasetAuthorizationLogic.decide(operations))
    }

    @Test
    fun `should deny access for non-owner user`() {
        val ownerId = "owner123"
        val requesterId = "requester456"
        val datasetDto = mockk<DatasetDto>()
        every { datasetDto.userId } returns ownerId
        every { authenticationFacade.userId } returns requesterId
        every { operations.returnObject } returns ResponseEntity.ok(datasetDto)

        assertFalse(getDatasetAuthorizationLogic.decide(operations))
    }

    @Test
    fun `should allow access when response status is not 2xx`() {
        every { operations.returnObject } returns ResponseEntity.status(HttpStatus.BAD_REQUEST).build<Any>()

        assertTrue(getDatasetAuthorizationLogic.decide(operations))
    }
}
