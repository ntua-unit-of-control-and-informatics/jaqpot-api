package org.jaqpot.api.storage.encoding

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Base64.isBase64
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.*

@Component
class FileEncodingProcessor {

    companion object {
        const val FILE_HEADER_SIZE = 6
    }

    private fun determineEncoding(bytes: ByteArray): Encoding {
        if (isLikelyBase64(bytes)) {
            return Encoding.BASE64
        }
        return Encoding.RAW
    }

    private fun isLikelyBase64(bytes: ByteArray?): Boolean {
        if (!isBase64(bytes)) {
            return false
        }

        try {
            Base64.getDecoder().decode(bytes)
            return true
        } catch (e: IllegalArgumentException) {
            return false
        }
    }

    private fun addMetadata(data: ByteArray): ByteArray {
        val metadata = FileMetadataV1(
            mapOf(
                "dataSize" to data.size.toString(),
            )
        )
        val metadataBytes = Json.encodeToString(metadata).toByteArray()


        val header =
            ByteBuffer.allocate(FILE_HEADER_SIZE) // 2 bytes for the metadata version, 4 bytes for the metadata size
                .putShort(metadata.version)
                .putInt(metadataBytes.size)

        return header.array() + metadataBytes + data
    }

    private fun readMetadata(data: ByteArray): FileMetadata {
        val version = data.sliceArray(0 until 2).toString().toShort()
        val metadataSize = data.sliceArray(2 until 6).toString().toInt()
        val metadataBytes = data.sliceArray(6 until 6 + metadataSize)
        if (version.toInt() == 1) {
            val metadata = Json.decodeFromString(FileMetadataV1.serializer(), String(metadataBytes))
            return metadata
        }
        throw IllegalArgumentException("Unsupported metadata version: $version")
    }

    fun prepareFileForStorage(data: ByteArray): ByteArray {
        var dataToStore = data;
        if (determineEncoding(data) == Encoding.BASE64) {
            dataToStore = Base64.getDecoder().decode(data)
        }

        return addMetadata(dataToStore)
    }

    fun readFile(data: ByteArray): ByteArray {
        if (determineEncoding(data) == Encoding.BASE64) {
            return Base64.getDecoder().decode(data)
        }

        val metadata = readMetadata(data)

        return data.sliceArray(FILE_HEADER_SIZE + metadata.getMetadataSize() until data.size)
    }
}
