package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.GetModels200ResponseDto
import org.jaqpot.api.model.UserDto
import org.springframework.data.domain.Page

fun Page<Model>.toGetModels200ResponseDto(creatorDto: UserDto?): GetModels200ResponseDto {
    return GetModels200ResponseDto(
        this.content.map { it.toDto(creatorDto) },
        this.totalElements.toInt(),
        this.totalPages,
        this.pageable.pageSize,
        this.pageable.pageNumber
    )
}
