package org.jaqpot.api.storage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.Model
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.storage.encoding.FileEncodingProcessor
import org.jaqpot.api.storage.s3.AWSS3Config
import org.springframework.stereotype.Service
import java.util.*

@Service
class StorageService(
    private val storage: Storage,
    private val fileEncodingProcessor: FileEncodingProcessor,
    private val awsS3Config: AWSS3Config
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val METADATA_VERSION = "1"
    }

    fun storeRawModel(model: Model): Boolean {
        try {
            this.storage.putObject(
                awsS3Config.modelsBucketName,
                model.id.toString(),
                model.actualModel!!,
                calculateMetadata(model)
            )
            return true
        } catch (e: Exception) {
            logger.error(e) { "Failed to store model with id ${model.id}" }
            return false
        }
    }

    private fun calculateMetadata(model: Model): Map<String, String> {
        return mapOf(
            "version" to METADATA_VERSION,
            "encoding" to fileEncodingProcessor.determineEncoding(model.actualModel!!).name
        )
    }

    fun readRawModel(model: Model): ByteArray {
        var rawModelFromStorage = Optional.empty<ByteArray>()
        try {
            rawModelFromStorage = this.storage.getObject(awsS3Config.modelsBucketName, model.id.toString())
        } catch (e: Exception) {
            logger.warn(e) { "Failed to read model with id ${model.id}" }
        }

        if (rawModelFromStorage.isPresent) {
            return fileEncodingProcessor.readFile(rawModelFromStorage.get(), model.id)
        } else if (model.actualModel != null) {
            logger.warn { "Failed to find raw model with id ${model.id} in storage, falling back to actual model from database" }
            return fileEncodingProcessor.readFile(model.actualModel!!, model.id)
        }

        throw JaqpotRuntimeException("Failed to find raw model with id ${model.id}")
    }
}
