package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Library
import org.jaqpot.api.model.LibraryDto
import org.jaqpot.api.model.ModelDto

fun Library.toDto(): LibraryDto {
    return LibraryDto(
        this.name,
        this.version,
        this.id,
        this.createdAt,
        this.updatedAt,
    )
}

fun LibraryDto.toEntity(model: ModelDto): Library {
    return Library(
        this.id,
        model.toEntity(),
        this.name,
        this.version
    )
}
