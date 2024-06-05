package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DatasetStatus
import org.jaqpot.api.model.DatasetDto

fun DatasetDto.Status.toEntity(): DatasetStatus {
    return when (this) {
        DatasetDto.Status.CREATED -> DatasetStatus.CREATED
        DatasetDto.Status.EXECUTING -> DatasetStatus.EXECUTING
        DatasetDto.Status.SUCCESS -> DatasetStatus.SUCCESS
        DatasetDto.Status.FAILURE -> DatasetStatus.FAILURE
    }
}

fun DatasetStatus.toDto(): DatasetDto.Status {
    return when (this) {
        DatasetStatus.CREATED -> DatasetDto.Status.CREATED
        DatasetStatus.EXECUTING -> DatasetDto.Status.EXECUTING
        DatasetStatus.SUCCESS -> DatasetDto.Status.SUCCESS
        DatasetStatus.FAILURE -> DatasetDto.Status.FAILURE
    }
}
