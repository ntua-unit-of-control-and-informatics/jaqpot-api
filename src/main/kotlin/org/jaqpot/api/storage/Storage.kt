package org.jaqpot.api.storage

import java.util.*

interface Storage {
    fun getObject(
        bucketName: String,
        keyName: String,
    ): Optional<ByteArray>

    fun getObjects(bucketName: String, keyNames: List<String>): Map<String, ByteArray>

    fun putObject(
        bucketName: String,
        keyName: String,
        obj: ByteArray,
        metadata: Map<String, String> = mapOf(),
    )

    fun deleteObject(
        bucketName: String,
        keyName: String
    )
}
