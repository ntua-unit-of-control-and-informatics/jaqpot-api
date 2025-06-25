package org.jaqpot.api.service.authentication.keycloak

import io.github.oshai.kotlinlogging.KotlinLogging
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Component

@Component
class KeycloakUserService(private val keycloakConfig: KeycloakConfig, private val keycloakAdminClient: Keycloak) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getUserById(id: String): UserRepresentation? {
        try {
            return keycloakAdminClient.realm(keycloakConfig.realm).users().get(id).toRepresentation()
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by id: $id" }
            return null
        }
    }

    fun getUserByUsername(username: String): UserRepresentation? {
        try {
            return keycloakAdminClient.realm(keycloakConfig.realm).users().searchByUsername(username, true).first()
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by username: $username" }
            return null
        }
    }

    fun getUserByEmail(email: String): UserRepresentation? {
        try {
            val users = keycloakAdminClient.realm(keycloakConfig.realm).users().searchByEmail(email, true)
            when (users.size) {
                0 -> {
                    return null
                }

                1 -> {
                    return users.first()
                }

                else -> {
                    logger.error { "Found more than 1 users with the same email" }
                    return null
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve user by email: $email" }
            return null
        }

    }

    data class PaginatedUsersResult(
        val users: List<UserRepresentation>,
        val totalCount: Int
    )

    fun getUsersPaginated(page: Int, size: Int, sortBy: String = "createdTimestamp", sortOrder: String = "desc"): PaginatedUsersResult {
        try {
            logger.debug { "Getting paginated users: page=$page, size=$size, sortBy=$sortBy, sortOrder=$sortOrder" }
            val usersResource = keycloakAdminClient.realm(keycloakConfig.realm).users()
            
            // Get total count first
            val totalCount = usersResource.count()
            logger.debug { "Total users count from Keycloak: $totalCount" }
            
            // Calculate proper offset
            val firstResult = page * size
            
            // Get all users (Keycloak doesn't support server-side sorting in the way we need)
            // For proper sorting, we need to get more users and sort them
            val allUsers = if (totalCount <= 1000) {
                // If total users is manageable, get all and sort properly
                usersResource.list(0, totalCount)
            } else {
                // For large datasets, get a larger chunk around our page
                val chunkSize = maxOf(size * 10, 100) // Get 10 pages worth or minimum 100
                val startIndex = maxOf(0, firstResult - (chunkSize / 2))
                usersResource.list(startIndex, chunkSize)
            }
            
            // Sort the users properly
            val sortedUsers = when (sortBy.lowercase()) {
                "email" -> if (sortOrder.lowercase() == "desc") 
                    allUsers.sortedByDescending { it.email ?: "" } 
                    else allUsers.sortedBy { it.email ?: "" }
                "username" -> if (sortOrder.lowercase() == "desc") 
                    allUsers.sortedByDescending { it.username ?: "" } 
                    else allUsers.sortedBy { it.username ?: "" }
                "firstname" -> if (sortOrder.lowercase() == "desc") 
                    allUsers.sortedByDescending { it.firstName ?: "" } 
                    else allUsers.sortedBy { it.firstName ?: "" }
                "lastname" -> if (sortOrder.lowercase() == "desc") 
                    allUsers.sortedByDescending { it.lastName ?: "" } 
                    else allUsers.sortedBy { it.lastName ?: "" }
                "createdat", "createdtimestamp" -> if (sortOrder.lowercase() == "desc") 
                    allUsers.sortedByDescending { it.createdTimestamp ?: 0L } 
                    else allUsers.sortedBy { it.createdTimestamp ?: 0L }
                else -> allUsers.sortedByDescending { it.createdTimestamp ?: 0L } // Default sort
            }
            
            // If we got all users, paginate from the sorted list
            val paginatedUsers = if (totalCount <= 1000) {
                sortedUsers.drop(firstResult).take(size)
            } else {
                // For large datasets, this is a compromise - may not be perfectly accurate
                sortedUsers.take(size)
            }
            
            return PaginatedUsersResult(paginatedUsers, totalCount)
            
        } catch (e: Exception) {
            logger.error(e) { "Could not retrieve paginated users" }
            return PaginatedUsersResult(emptyList(), 0)
        }
    }

}
