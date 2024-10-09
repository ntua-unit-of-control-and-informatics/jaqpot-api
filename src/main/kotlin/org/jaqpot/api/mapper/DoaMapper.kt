package org.jaqpot.api.mapper

import com.google.gson.Gson
import org.jaqpot.api.dto.prediction.PredictionDoaDto
import org.jaqpot.api.entity.Doa
import org.jaqpot.api.entity.Model
import org.jaqpot.api.model.DoaDataDto
import org.jaqpot.api.model.DoaDto


//fun List<Doa>.toDto(): ModelDoaDto {
//    return ModelDoaDto(
//        leverage = this.find { it.method == DoaMethod.LEVERAGE }
//            ?.let { LeverageDoaDto(id = it.id, hStar = 0f, data = listOf()) },
//        boundingBox = this.find { it.method == DoaMethod.BOUNDING_BOX }
//            ?.let { BoundingBoxDoaDto(id = it.id, data = listOf()) },
//        kernelBased = this.find { it.method == DoaMethod.KERNEL_BASED }
//            ?.let { KernelBasedDoaDto(id = it.id, data = listOf()) },
//        meanVar = this.find { it.method == DoaMethod.MEAN_VAR }?.let { MeanVarDoaDto(id = it.id, data = listOf()) },
//        mahalanobis = this.find { it.method == DoaMethod.MAHALANOBIS }
//            ?.let { MahalanobisDoaDto(id = it.id, data = listOf()) },
//        cityBlock = this.find { it.method == DoaMethod.CITY_BLOCK }
//            ?.let { CityBlockDoaDto(id = it.id, data = listOf()) }
//    )
//}

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

//fun LeverageDoaDto.toEntity(model: Model): Doa {
//    return Doa(
//        id,
//        model,
//        DoaMethod.LEVERAGE,
//        Json.encodeToString(this).toByteArray(),
//    )
//}
//
//fun BoundingBoxDoaDto.toEntity(model: Model): Doa {
//    return Doa(
//        id,
//        model,
//        DoaMethod.BOUNDING_BOX,
//        Json.encodeToString(this).toByteArray(),
//    )
//}
//
//fun Doa.toDto(rawDoa: ByteArray): DoaDto {
//    return DoaDto(
//        method = this.method.toDto(),
//        rawDoa = rawDoa,
//        id = this.id,
//        createdAt = this.createdAt,
//        updatedAt = this.updatedAt,
//    )
//}
//
//fun MeanVarDoaDto.toEntity(model: Model): Doa {
//    return Doa(
//        id,
//        model,
//        DoaMethod.MEAN_VAR,
//        Json.encodeToString(this).toByteArray(),
//    )
//}
//
//fun MahalanobisDoaDto.toEntity(model: Model): Doa {
//    return Doa(
//        id,
//        model,
//        DoaMethod.MAHALANOBIS,
//        Json.encodeToString(this).toByteArray(),
//    )
//}
//
//fun CityBlockDoaDto.toEntity(model: Model): Doa {
//    return Doa(
//        id,
//        model,
//        DoaMethod.CITY_BLOCK,
//        Json.encodeToString(this).toByteArray(),
//    )
//}

