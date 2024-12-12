package org.jaqpot.api.storage

import org.jaqpot.api.error.JaqpotRuntimeException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Profile("local")
@Service
class LocalStorage : Storage {
    override fun getObject(bucketName: String, keyName: String): Optional<ByteArray> {
        return Optional.empty()
    }

    override fun getObjects(bucketName: String, keyNames: List<String>): Map<String, ByteArray> {
        return emptyMap()
    }

    override fun listObjects(bucketName: String, prefix: String): List<String> {
        return emptyList()
    }

    override fun putObject(bucketName: String, keyName: String, obj: ByteArray, metadata: Map<String, String>) {
        throw JaqpotRuntimeException("Not implemented")
    }

    override fun putObject(
        bucketName: String,
        keyName: String,
        contentType: String,
        obj: ByteArray,
        metadata: Map<String, String>
    ) {
        throw JaqpotRuntimeException("Not implemented")
    }

    override fun deleteObject(bucketName: String, keyName: String) {
        throw JaqpotRuntimeException("Not implemented")
    }
}
