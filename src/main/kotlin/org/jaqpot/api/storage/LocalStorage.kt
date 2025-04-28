package org.jaqpot.api.storage

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Profile("local")
@Service
class LocalStorage : Storage {
    // Structure: Map<BucketName, Map<KeyName, StorageObject>>
    private val storage = ConcurrentHashMap<String, ConcurrentHashMap<String, StorageObject>>()

    data class StorageObject(
        val data: ByteArray,
        val contentType: String = "application/octet-stream",
        val metadata: Map<String, String> = emptyMap()
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StorageObject

            if (!data.contentEquals(other.data)) return false
            if (contentType != other.contentType) return false
            if (metadata != other.metadata) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + contentType.hashCode()
            result = 31 * result + metadata.hashCode()
            return result
        }
    }

    override fun getObject(bucketName: String, keyName: String): Optional<ByteArray> {
        return Optional.ofNullable(
            storage[bucketName]?.get(keyName)?.data
        )
    }

    override fun getObjects(bucketName: String, keyNames: List<String>): Map<String, ByteArray> {
        return storage[bucketName]?.let { bucket ->
            keyNames.mapNotNull { keyName ->
                bucket[keyName]?.let { obj ->
                    keyName to obj.data
                }
            }.toMap()
        } ?: emptyMap()
    }

    override fun listObjects(bucketName: String, prefix: String): List<String> {
        return storage[bucketName]?.keys
            ?.filter { it.startsWith(prefix) }
            ?.toList()
            ?: emptyList()
    }

    override fun putObject(bucketName: String, keyName: String, obj: ByteArray, metadata: Map<String, String>) {
        putObject(bucketName, keyName, "application/octet-stream", obj, metadata)
    }

    override fun putObject(
        bucketName: String,
        keyName: String,
        contentType: String,
        obj: ByteArray,
        metadata: Map<String, String>
    ) {
        val bucket = storage.computeIfAbsent(bucketName) { ConcurrentHashMap() }
        bucket[keyName] = StorageObject(obj, contentType, metadata)
    }

    override fun deleteObject(bucketName: String, keyName: String) {
        storage[bucketName]?.remove(keyName)
    }

    override fun getPreSignedUploadUrl(bucketName: String, keyName: String, metadata: Map<String, String>): String {
        TODO("Not yet implemented")
    }

    // Additional helper methods for testing
    fun clearStorage() {
        storage.clear()
    }

    fun getBuckets(): Set<String> {
        return storage.keys
    }

    fun getObjectMetadata(bucketName: String, keyName: String): Map<String, String>? {
        return storage[bucketName]?.get(keyName)?.metadata
    }

    fun getObjectContentType(bucketName: String, keyName: String): String? {
        return storage[bucketName]?.get(keyName)?.contentType
    }
}
