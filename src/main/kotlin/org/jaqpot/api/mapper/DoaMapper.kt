package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Doa
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DoaDto

fun Doa.toDto(rawDoa: ByteArray): DoaDto {
    return DoaDto(
        method = this.method.toDto(),
        rawDoa = rawDoa,
        id = this.id,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
}

fun DoaDto.toEntity(model: Model): Doa {
    return Doa(
        this.id,
        model,
        this.method.toEntity(),
        this.rawDoa,
    )
}
