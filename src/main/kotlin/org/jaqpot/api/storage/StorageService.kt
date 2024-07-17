package org.jaqpot.api.storage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.Model
import org.jaqpot.api.storage.encoding.FileEncodingProcessor
import org.springframework.stereotype.Service
import java.util.*

@Service
class StorageService(private val storage: Storage, private val fileEncodingProcessor: FileEncodingProcessor) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun storeRawModel(model: Model) {
        val rawModel = model.actualModel
        try {
            this.storage.putObject(
                "models",
                model.id.toString(),
                fileEncodingProcessor.prepareFileForStorage(model.actualModel!!)
            )
        } catch (e: Exception) {
            logger.error { "Failed to store model with id ${model.id}" }
        }
    }

    fun readRawModel(model: Model): Optional<ByteArray> {
        val rawModel = this.storage.getObject("models", model.id.toString())
        if (rawModel.isPresent) {
            return Optional.of(fileEncodingProcessor.readFile(rawModel.get()))
        }

        return Optional.empty()
    }
}
