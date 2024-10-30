package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Model
import org.jaqpot.api.entity.ModelTransformer
import org.jaqpot.api.entity.ModelTransformerType
import org.jaqpot.api.model.TransformerDto

fun ModelTransformer.toDto(): TransformerDto {
    return TransformerDto(
        this.name,
        this.config
    )
}

fun TransformerDto.toEntity(model: Model, type: ModelTransformerType): ModelTransformer {
    return ModelTransformer(
        this.id,
        model = model,
        transformerType = type,
        name = this.name,
        config = this.config
    )
}
