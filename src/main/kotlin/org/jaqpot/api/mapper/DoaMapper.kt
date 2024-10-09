package org.jaqpot.api.mapper

import com.google.gson.Gson
import org.jaqpot.api.dto.prediction.PredictionDoaDto
import org.jaqpot.api.entity.Doa
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DoaDataDto
import org.jaqpot.api.model.DoaDto

fun Doa.toDto(): DoaDto {
    return DoaDto(
        id = this.id,
        method = this.method.toDto(),
        data = DoaDataDto(),
    )
}

fun DoaDto.toEntity(model: Model): Doa {
    return Doa(
        id = this.id,
        model = model,
        method = this.method.toEntity(),
        rawDoa = Gson().toJson(this.data).toByteArray(),
    )
}

fun Doa.toPredictionDto(doaData: Any): PredictionDoaDto {
    return PredictionDoaDto(
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        id = this.id,
        method = this.method,
        doaData = doaData
    )
}

