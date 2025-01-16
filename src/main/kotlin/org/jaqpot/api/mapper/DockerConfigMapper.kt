package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DockerConfig
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DockerConfigDto

fun DockerConfig.toDto(): DockerConfigDto {
    return DockerConfigDto(
        appName = this.appName,
        dockerImage = this.dockerImage
    )
}

fun DockerConfigDto.toEntity(model: Model): DockerConfig {
    return DockerConfig(
        appName = this.appName,
        model = model,
        dockerImage = this.dockerImage,
        llmModelId = this.llmModelId
    )
}
