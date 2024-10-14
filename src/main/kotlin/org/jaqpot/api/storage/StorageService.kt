package org.jaqpot.api.storage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.entity.Dataset
import org.jaqpot.api.entity.Doa
import org.jaqpot.api.entity.Model
import org.jaqpot.api.error.JaqpotRuntimeException
import org.jaqpot.api.storage.encoding.Encoding
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

    fun storeDataset(dataset: Dataset): Boolean {
        try {
            val metadata = mapOf(
                "version" to METADATA_VERSION,
                "encoding" to Encoding.RAW.name,
                "modelId" to dataset.model.id.toString(),
                "userId" to dataset.userId,
                "type" to dataset.type.name,
                "entryType" to dataset.entryType.name,
            )

            this.storage.putObject(
                awsS3Config.datasetsBucketName,
                "${dataset.id.toString()}/input",
                Gson().toJson(dataset.input).toByteArray(),
                metadata
            )

            this.storage.putObject(
                awsS3Config.datasetsBucketName,
                "${dataset.id.toString()}/result",
                Gson().toJson(dataset.result).toByteArray(),
                metadata
            )

            return true;
        } catch (e: Exception) {
            logger.error(e) { "Failed to store dataset with id ${dataset.id}" }
            return false
        }
    }

    fun storeDoa(doa: Doa): Boolean {
        try {
            val metadata = mapOf(
                "version" to METADATA_VERSION,
                "encoding" to Encoding.RAW.name,
                "modelId" to doa.model.id.toString(),
                "method" to doa.method.name
            )

            this.storage.putObject(
                awsS3Config.doasBucketName,
                getDoaStorageKey(doa),
                doa.rawDoa!!,
                metadata
            )
            return true
        } catch (e: Exception) {
            logger.error(e) { "Failed to store doa with id ${doa.id}" }
            return false
        }
    }

    private fun getDoaStorageKey(doa: Doa) = "${doa.model.id}/${doa.method.name}"

    fun storeRawModel(model: Model): Boolean {
        try {
            val metadata = mapOf(
                "version" to METADATA_VERSION,
                "encoding" to fileEncodingProcessor.determineEncoding(model.rawModel!!).name
            )

            this.storage.putObject(
                awsS3Config.modelsBucketName,
                getModelStorageKey(model),
                model.rawModel!!,
                metadata
            )
            return true
        } catch (e: Exception) {
            logger.error(e) { "Failed to store model with id ${model.id}" }
            return false
        }
    }

    fun readRawModel(model: Model): ByteArray {
        var rawModelFromStorage = Optional.empty<ByteArray>()
        try {
            rawModelFromStorage = this.storage.getObject(awsS3Config.modelsBucketName, getModelStorageKey(model))
        } catch (e: Exception) {
            logger.warn(e) { "Failed to read model with id ${model.id}" }
        }

        if (rawModelFromStorage.isPresent) {
            return fileEncodingProcessor.readFile(rawModelFromStorage.get(), model.id)
        } else if (model.rawModel != null) {
            logger.warn { "Failed to find raw model with id ${model.id} in storage, falling back to raw model from database" }
            return fileEncodingProcessor.readFile(model.rawModel!!, model.id)
        }

        throw JaqpotRuntimeException("Failed to find raw model with id ${model.id}")
    }

    private fun getModelStorageKey(model: Model) = model.id.toString()


    fun readRawDoa(doa: Doa): ByteArray {
        var rawDoaFromStorage = Optional.empty<ByteArray>()
        try {
            rawDoaFromStorage = this.storage.getObject(awsS3Config.doasBucketName, getDoaStorageKey(doa))
        } catch (e: Exception) {
            logger.warn(e) { "Failed to read doa with id ${doa.id}" }
        }

        if (rawDoaFromStorage.isPresent) {
            return rawDoaFromStorage.get()
        } else if (doa.rawDoa != null) {
            logger.warn { "Failed to find raw doa with id ${doa.id} in storage, falling back to raw doa from database" }
            return doa.rawDoa!!
        }

        throw JaqpotRuntimeException("Failed to find raw doa with id ${doa.id}")
    }

    fun readRawDatasetInput(dataset: Dataset): List<Any> {
        var rawDatasetFromStorage = Optional.empty<ByteArray>()
        try {
            rawDatasetFromStorage =
                this.storage.getObject(awsS3Config.datasetsBucketName, "${dataset.id.toString()}/input")
        } catch (e: Exception) {
            logger.warn(e) { "Failed to read dataset with id ${dataset.id}" }
        }

        if (rawDatasetFromStorage.isPresent) {
            val rawData = rawDatasetFromStorage.get()
            val type = object : TypeToken<List<Any>>() {}.type
            val input: List<Any> = Gson().fromJson(rawData.decodeToString(), type)
            return input
        } else if (dataset.input != null) {
            logger.warn { "Failed to find raw dataset with id ${dataset.id} in storage, falling back to raw dataset from database" }
            return dataset.input
        }

        throw JaqpotRuntimeException("Failed to find raw dataset input with id ${dataset.id}")
    }

    fun readRawDatasetResult(dataset: Dataset): List<Any>? {
        var rawDatasetFromStorage = Optional.empty<ByteArray>()
        try {
            rawDatasetFromStorage =
                this.storage.getObject(awsS3Config.datasetsBucketName, "${dataset.id.toString()}/result")
        } catch (e: Exception) {
            logger.warn(e) { "Failed to read dataset with id ${dataset.id}" }
        }

        if (rawDatasetFromStorage.isPresent) {
            val rawData = rawDatasetFromStorage.get()
            val type = object : TypeToken<List<Any>>() {}.type
            val result: List<Any> = Gson().fromJson(rawData.decodeToString(), type)
            return result
        } else if (dataset.result != null) {
            logger.warn { "Failed to find raw dataset with id ${dataset.id} in storage, falling back to raw dataset from database" }
            return dataset.result!!
        }

        return null
    }
}
