package org.jaqpot.api.service.admin

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.AdminApiDelegate
import org.jaqpot.api.mapper.UserMapper
import org.jaqpot.api.mapper.toGetAllModels200ResponseDto
import org.jaqpot.api.model.GetAllModels200ResponseDto
import org.jaqpot.api.model.GetUsers200ResponseDto
import org.jaqpot.api.model.UserDto
import org.jaqpot.api.repository.ModelRepository
import org.jaqpot.api.service.authentication.AuthenticationFacade
import org.jaqpot.api.service.authentication.UserService
import org.jaqpot.api.service.authentication.keycloak.KeycloakUserService
import org.jaqpot.api.service.ratelimit.WithRateLimitProtectionByUser
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class AdminService(
    private val authenticationFacade: AuthenticationFacade,
    private val keycloakUserService: KeycloakUserService,
    private val userMapper: UserMapper,
    private val modelRepository: ModelRepository,
    private val userService: UserService
) : AdminApiDelegate {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PreAuthorize("@authenticationFacade.isAdmin or @authenticationFacade.isUpciUser")
    @WithRateLimitProtectionByUser(limit = 20, intervalInSeconds = 60)
    override fun getUsers(pageable: Pageable): ResponseEntity<GetUsers200ResponseDto> {
        val page = pageable.pageNumber
        val size = pageable.pageSize
        logger.info { "Admin user ${authenticationFacade.userId} requested users page $page with size $size" }

        try {
            val (sortBy, sortOrder) = if (pageable.sort.isSorted) {
                val order = pageable.sort.first()
                Pair(order.property, order.direction.name.lowercase())
            } else {
                Pair("createdTimestamp", "desc")
            }

            val result = keycloakUserService.getUsersPaginated(page, size, sortBy, sortOrder)
            
            val totalPages = (result.totalCount + size - 1) / size
            val numberOfElements = result.users.size
            
            val response = GetUsers200ResponseDto(
                content = result.users.map { userMapper.generateUserDto(it) },
                pageable = org.jaqpot.api.model.GetUsers200ResponsePageableDto(
                    sort = org.jaqpot.api.model.GetUsers200ResponsePageableSortDto(
                        empty = !pageable.sort.isSorted,
                        sorted = pageable.sort.isSorted,
                        unsorted = !pageable.sort.isSorted
                    ),
                    offset = page * size,
                    pageSize = size,
                    pageNumber = page,
                    unpaged = false,
                    paged = true
                ),
                last = page >= totalPages - 1,
                totalPages = totalPages,
                totalElements = result.totalCount,
                propertySize = size,
                number = page,
                sort = org.jaqpot.api.model.GetUsers200ResponsePageableSortDto(
                    empty = !pageable.sort.isSorted,
                    sorted = pageable.sort.isSorted,
                    unsorted = !pageable.sort.isSorted
                ),
                first = page == 0,
                numberOfElements = numberOfElements,
                empty = result.users.isEmpty()
            )
            
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error(e) { "Error retrieving users for admin ${authenticationFacade.userId}" }
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve users")
        }
    }

    @PreAuthorize("@authenticationFacade.isAdmin or @authenticationFacade.isUpciUser")
    @WithRateLimitProtectionByUser(limit = 20, intervalInSeconds = 60)
    override fun getAllModels(pageable: Pageable): ResponseEntity<GetAllModels200ResponseDto> {
        logger.info { "Admin user ${authenticationFacade.userId} requested all models page ${pageable.pageNumber} with size ${pageable.pageSize}" }

        try {
            val modelsPage = modelRepository.findAll(pageable)
            val modelIdToUserMap = modelsPage.content.associateBy(
                { it.id!! },
                { userService.getUserById(it.creatorId).orElse(UserDto(it.creatorId)) }
            )

            return ResponseEntity.ok().body(modelsPage.toGetAllModels200ResponseDto(modelIdToUserMap))
        } catch (e: Exception) {
            logger.error(e) { "Error retrieving all models for admin ${authenticationFacade.userId}" }
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve models")
        }
    }
}