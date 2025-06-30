package org.jaqpot.api.storage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jaqpot.api.error.JaqpotRuntimeException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Profile("local")
@Service
class LocalStorage : Storage {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun getObject(bucketName: String, keyName: String): Optional<ByteArray> {
        logger.info { "Local storage: Simulating S3 failure for getObject, returning empty Optional to fall back to database storage" }
        return Optional.empty()
    }

    override fun getObjects(bucketName: String, keyNames: List<String>): Map<String, ByteArray> {
        logger.info { "Local storage: Simulating S3 failure for getObjects, returning empty map to fall back to database storage" }
        return emptyMap()
    }

    override fun listObjects(bucketName: String, prefix: String): List<String> {
        logger.info { "Local storage: Simulating S3 failure for listObjects, returning empty list" }
        return emptyList()
    }

    override fun putObject(bucketName: String, keyName: String, obj: ByteArray, metadata: Map<String, String>) {
        logger.info { "Local storage: Simulating S3 failure for putObject, data will remain in database" }
        throw JaqpotRuntimeException("Failing so object can be stored in database")
    }

    override fun putObject(
        bucketName: String,
        keyName: String,
        contentType: String,
        obj: ByteArray,
        metadata: Map<String, String>
    ) {
        logger.info { "Local storage: Simulating S3 failure for putObject with content type, data will remain in database" }
        throw JaqpotRuntimeException("Failing so object can be stored in database")
    }

    override fun deleteObject(bucketName: String, keyName: String) {
        logger.info { "Local storage: Simulating S3 failure for deleteObject" }
        // Do nothing
    }

    override fun getPreSignedUploadUrl(bucketName: String, keyName: String, metadata: Map<String, String>): String {
        logger.info { "Local storage: Returning dummy pre-signed URL for local development" }
        return "local://$bucketName/$keyName"
    }

    override fun getObjectContentLength(bucketName: String, keyName: String): Optional<Long> {
        logger.info { "Local storage: Simulating S3 failure for getObjectMetadata" }
        return Optional.of(10L)
    }

    override fun getPreSignedDownloadUrl(bucketName: String, keyName: String, expirationMinutes: Int): String {
        logger.info { "Local storage: Returning dummy pre-signed download URL for local development" }
        return "local://$bucketName/$keyName?expires=${System.currentTimeMillis() + (expirationMinutes * 60 * 1000)}"
    }
}
