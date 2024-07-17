package org.jaqpot.api.storage.encoding

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class FileMetadataV1(
    val metadata: Map<String, String> = mapOf(),
    val version: Short = 1,
) : FileMetadata {
    override fun getMetadataSize(): Int {
        return Json.encodeToString(this).toByteArray().size
    }
}
