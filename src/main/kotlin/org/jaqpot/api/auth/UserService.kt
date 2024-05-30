package org.jaqpot.api.auth

interface UserService {
    fun getUserById(id: String): UserDto
    fun getUserByUsername(username: String): UserDto
    fun getUserByEmail(email: String): UserDto
}
