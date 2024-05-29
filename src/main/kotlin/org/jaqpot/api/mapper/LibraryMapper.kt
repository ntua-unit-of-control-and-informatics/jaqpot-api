package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Library
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.LibraryDto

fun Library.toDto(): LibraryDto {
    return LibraryDto(
        this.name,
        this.version,
        this.id,
        this.createdAt,
        this.updatedAt,
    )
}

fun LibraryDto.toEntity(model: Model): Library {
    return Library(
        this.id,
        model,
        this.name,
        this.version
    )
}
