package org.jaqpot.api.mapper

import org.jaqpot.api.entity.DoaMethod
import org.jaqpot.api.model.DoaDto

fun DoaDto.Method.toEntity(): DoaMethod {
    return when (this) {
        DoaDto.Method.LEVERAGE -> DoaMethod.LEVERAGE
        DoaDto.Method.BOUNDING_BOX -> DoaMethod.BOUNDING_BOX
        DoaDto.Method.KERNEL_BASED -> DoaMethod.KERNEL_BASED
        DoaDto.Method.MEAN_VAR -> DoaMethod.MEAN_VAR
        DoaDto.Method.MAHALANOBIS -> DoaMethod.MAHALANOBIS
        DoaDto.Method.CITY_BLOCK -> DoaMethod.CITY_BLOCK
    }
}

fun DoaMethod.toDto(): DoaDto.Method {
    return when (this) {
        DoaMethod.LEVERAGE -> DoaDto.Method.LEVERAGE
        DoaMethod.BOUNDING_BOX -> DoaDto.Method.BOUNDING_BOX
        DoaMethod.KERNEL_BASED -> DoaDto.Method.KERNEL_BASED
        DoaMethod.MEAN_VAR -> DoaDto.Method.MEAN_VAR
        DoaMethod.MAHALANOBIS -> DoaDto.Method.MAHALANOBIS
        DoaMethod.CITY_BLOCK -> DoaDto.Method.CITY_BLOCK
    }
}

