package org.jaqpot.api.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.model.DatasetDto
import org.jaqpot.api.model.PredictionModelDto
import org.jaqpot.api.model.PredictionResponseDto
import org.jaqpot.api.repository.DatasetPredictionCacheRepository
import org.jaqpot.api.storage.StorageService
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.*

@Service
class DatasetCachedPredictionService(
    private val datasetPredictionCacheRepository: DatasetPredictionCacheRepository,
    private val objectMapper: ObjectMapper,
    private val storageService: StorageService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getPredictionResults(
        predictionModelDto: PredictionModelDto,
        inputDatasetDto: DatasetDto
    ): Optional<PredictionResponseDto> {
        // Generate hash for input dataset
        val inputHash = generateInputHash(inputDatasetDto)

        // Try to find cached prediction
        val cachedPrediction = datasetPredictionCacheRepository
            .findByModelIdAndInputHash(predictionModelDto.id.toString(), inputHash)

        if (cachedPrediction.isPresent) {
            logger.info { "Found cached prediction for model ${predictionModelDto.id} with input hash $inputHash" }
            val result = storageService.readRawDatasetResult(cachedPrediction.get().dataset)
            return Optional.of(PredictionResponseDto(result!!))
        }

        return Optional.empty()
    }

    private fun generateInputHash(datasetDto: DatasetDto): String {
        val inputJson = objectMapper.writeValueAsString(datasetDto.input)
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(inputJson.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

}
