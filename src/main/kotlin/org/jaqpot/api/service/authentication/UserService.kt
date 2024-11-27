package org.jaqpot.api.service.authentication

import org.jaqpot.api.model.UserDto
import java.util.*

interface UserService {
    fun getUserById(id: String): Optional<UserDto>
    fun getUserByEmail(email: String): Optional<UserDto>
    fun getUserByUsername(username: String): Optional<UserDto>
}
