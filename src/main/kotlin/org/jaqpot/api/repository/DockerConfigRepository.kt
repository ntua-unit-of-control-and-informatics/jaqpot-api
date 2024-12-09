package org.jaqpot.api.repository

import org.jaqpot.api.entity.DockerConfig
import org.springframework.data.repository.CrudRepository
import java.util.*

interface DockerConfigRepository : CrudRepository<DockerConfig, Long> {
    fun findByModelId(modelId: Long): Optional<DockerConfig>
}
