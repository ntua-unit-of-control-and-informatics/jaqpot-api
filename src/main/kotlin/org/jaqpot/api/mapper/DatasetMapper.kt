package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.UserDto


fun Dataset.toDto(userDto: UserDto): DatasetDto {
    return DatasetDto(
        this.type.toDto(),
        this.dataEntries.map { it.toDto(userDto) },
        this.id,
        this.createdAt,
        this.updatedAt
    )
}

fun DatasetDto.toEntity(model: Model, userId: String): Dataset {
    val d = Dataset(
        this.id,
        model,
        userId,
        this.type.toEntity(),
        mutableListOf()
    )

    d.dataEntries.addAll(this.dataEntries.map { it -> it.toEntity(d, userId) })
    
    return d;
}
