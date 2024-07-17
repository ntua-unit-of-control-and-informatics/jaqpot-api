package org.jaqpot.api.storage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.Model
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.storage.encoding.FileEncodingProcessor
import org.springframework.stereotype.Service
import java.util.*

@Service
class StorageService(private val storage: Storage, private val fileEncodingProcessor: FileEncodingProcessor) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun storeRawModel(model: Model): Boolean {
        try {
            this.storage.putObject(
                "models",
                model.id.toString(),
                fileEncodingProcessor.prepareFileForStorage(model.actualModel!!)
            )
            return true
        } catch (e: Exception) {
            logger.error { "Failed to store model with id ${model.id}" }
            return false
        }
    }

    fun readRawModel(model: Model): ByteArray {
        var rawModelFromStorage = Optional.empty<ByteArray>()
        try {
            rawModelFromStorage = this.storage.getObject("models", model.id.toString())
        } catch (e: Exception) {
            logger.warn { "Failed to read model with id ${model.id}" }
        }

        if (rawModelFromStorage.isPresent) {
            return fileEncodingProcessor.readFile(rawModelFromStorage.get())
        } else if (model.actualModel != null) {
            logger.warn { "Failed to find raw model with id ${model.id} in storage, falling back to actual model from database" }
            return fileEncodingProcessor.readFile(model.actualModel!!, fromDatabase = true)
        }
        
        throw JaqpotRuntimeException("Failed to find raw model with id ${model.id}")
    }
}
