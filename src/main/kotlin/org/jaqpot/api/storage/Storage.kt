package org.jaqpot.api.storage

import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import java.util.*

interface Storage {
    fun getObject(
        bucketName: String,
        keyName: String,
    ): Optional<ByteArray>

    fun getObjectMetadata(bucketName: String, keyName: String): HeadObjectResponse

    fun getObjects(bucketName: String, keyNames: List<String>): Map<String, ByteArray>

    fun listObjects(bucketName: String, prefix: String): List<String>

    fun putObject(
        bucketName: String,
        keyName: String,
        obj: ByteArray,
        metadata: Map<String, String> = mapOf(),
    )

    fun putObject(
        bucketName: String,
        keyName: String,
        contentType: String,
        obj: ByteArray,
        metadata: Map<String, String> = mapOf(),
    )

    fun deleteObject(
        bucketName: String,
        keyName: String
    )

    fun getPreSignedUploadUrl(bucketName: String, keyName: String, metadata: Map<String, String> = emptyMap()): String
}
