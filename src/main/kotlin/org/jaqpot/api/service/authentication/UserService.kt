package org.jaqpot.api.service.authentication

import org.jaqpot.api.model.UserDto

interface UserService {
    fun getUserById(id: String): UserDto
    fun getUserByUsername(username: String): UserDto
    fun getUserByEmail(email: String): UserDto
}
