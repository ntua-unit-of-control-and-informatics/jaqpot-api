package org.jaqpot.api.repository

import org.jaqpot.api.entity.ApiKey
import org.springframework.data.repository.CrudRepository

interface ApiKeyRepository : CrudRepository<ApiKey, String> {
    fun findByClientKey(clientKey: String): ApiKey?
}
