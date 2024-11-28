package org.jaqpot.api.storage.encoding

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.codec.binary.Base64.isBase64
import org.springframework.stereotype.Component
import java.util.*

@Component
class FileEncodingProcessor {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun determineEncoding(bytes: ByteArray): Encoding {
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

    fun readFile(data: ByteArray, objectId: String?): ByteArray {
        if (determineEncoding(data) == Encoding.BASE64) {
            logger.warn { "Deprecated base64 encoding detected on object $objectId" }
            // no metadata exist on legacy base64 encoded files
            return Base64.getDecoder().decode(data)
        }

        return data
    }
}
