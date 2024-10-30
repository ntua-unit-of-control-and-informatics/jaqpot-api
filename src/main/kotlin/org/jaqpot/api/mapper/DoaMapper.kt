package org.jaqpot.api.mapper

import com.google.gson.Gson
import org.jaqpot.api.entity.Doa
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DoaDataDto
import org.jaqpot.api.model.DoaDto

fun Doa.toDto(doaData: DoaDataDto): DoaDto {
    return DoaDto(
        id = this.id,
        method = this.method.toDto(),
        data = doaData,
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
