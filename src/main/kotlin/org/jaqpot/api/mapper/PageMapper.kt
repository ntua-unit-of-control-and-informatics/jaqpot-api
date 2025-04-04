package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.GetDatasets200ResponseDto
import org.jaqpot.api.model.GetModels200ResponseDto
import org.jaqpot.api.model.UserDto
import org.springframework.data.domain.Page

fun Page<Model>.toGetModels200ResponseDto(modelToUserMap: Map<Long, UserDto>?): GetModels200ResponseDto {
    return GetModels200ResponseDto(
        this.content.map { it.toModelSummaryDto(modelToUserMap?.get(it.id)) },
        this.totalElements.toInt(),
        this.totalPages,
        this.pageable.pageSize,
        this.pageable.pageNumber
    )
}

fun Page<Dataset>.toGetDatasets200ResponseDto(
    inputs: Map<String, List<Any>>,
    results: Map<String, List<Any>?>
): GetDatasets200ResponseDto {


    return GetDatasets200ResponseDto(
        this.content.map {
            it.toDto(
                inputs[it.id.toString()] ?: emptyList(),
                results[it.id.toString()] ?: emptyList()
            )
        }, // return empty input and result for datasets page
        this.totalElements.toInt(),
        this.totalPages,
        this.pageable.pageSize,
        this.pageable.pageNumber
    )
}
