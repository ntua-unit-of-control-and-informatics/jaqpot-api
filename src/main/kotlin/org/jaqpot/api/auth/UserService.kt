package org.jaqpot.api.auth

import org.jaqpot.api.model.UserDto

interface UserService {
    fun getUserById(id: String): UserDto
    fun getUserByUsername(username: String): UserDto
    fun getUserByEmail(email: String): UserDto
}
