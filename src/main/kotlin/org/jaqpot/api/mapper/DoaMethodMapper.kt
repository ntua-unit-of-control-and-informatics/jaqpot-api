package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DoaMethod
import org.jaqpot.api.model.DoaMethodDto

fun DoaMethodDto.toEntity(): DoaMethod {
    return when (this) {
        DoaMethodDto.LEVERAGE -> DoaMethod.LEVERAGE
        DoaMethodDto.BOUNDING_BOX -> DoaMethod.BOUNDING_BOX
        DoaMethodDto.KERNEL_BASED -> DoaMethod.KERNEL_BASED
        DoaMethodDto.MEAN_VAR -> DoaMethod.MEAN_VAR
        DoaMethodDto.MAHALANOBIS -> DoaMethod.MAHALANOBIS
        DoaMethodDto.CITY_BLOCK -> DoaMethod.CITY_BLOCK
    }
}

fun DoaMethod.toDto(): DoaMethodDto {
    return when (this) {
        DoaMethod.LEVERAGE -> DoaMethodDto.LEVERAGE
        DoaMethod.BOUNDING_BOX -> DoaMethodDto.BOUNDING_BOX
        DoaMethod.KERNEL_BASED -> DoaMethodDto.KERNEL_BASED
        DoaMethod.MEAN_VAR -> DoaMethodDto.MEAN_VAR
        DoaMethod.MAHALANOBIS -> DoaMethodDto.MAHALANOBIS
        DoaMethod.CITY_BLOCK -> DoaMethodDto.CITY_BLOCK
    }
}

