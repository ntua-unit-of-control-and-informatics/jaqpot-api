package org.jaqpot.api.mapper

import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.GetAllModels200ResponseDto
import org.jaqpot.api.model.GetDatasets200ResponseDto
import org.jaqpot.api.model.GetModels200ResponseDto
import org.jaqpot.api.model.GetUsers200ResponsePageableDto
import org.jaqpot.api.model.GetUsers200ResponsePageableSortDto
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

fun Page<Model>.toGetAllModels200ResponseDto(modelToUserMap: Map<Long, UserDto>?): GetAllModels200ResponseDto {
    return GetAllModels200ResponseDto(
        content = this.content.map { it.toModelSummaryDto(modelToUserMap?.get(it.id)) },
        pageable = GetUsers200ResponsePageableDto(
            sort = GetUsers200ResponsePageableSortDto(
                empty = this.sort.isEmpty,
                sorted = this.sort.isSorted,
                unsorted = this.sort.isUnsorted
            ),
            offset = this.pageable.offset.toInt(),
            pageSize = this.pageable.pageSize,
            pageNumber = this.pageable.pageNumber,
            unpaged = this.pageable.isUnpaged,
            paged = this.pageable.isPaged
        ),
        last = this.isLast,
        totalPages = this.totalPages,
        totalElements = this.totalElements.toInt(),
        propertySize = this.size,
        number = this.number,
        sort = GetUsers200ResponsePageableSortDto(
            empty = this.sort.isEmpty,
            sorted = this.sort.isSorted,
            unsorted = this.sort.isUnsorted
        ),
        first = this.isFirst,
        numberOfElements = this.numberOfElements,
        empty = this.isEmpty
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
