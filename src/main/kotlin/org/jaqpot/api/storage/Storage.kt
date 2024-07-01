package org.jaqpot.api.storage

import java.util.*

interface Storage {
    fun getObject(
        bucketName: String,
        keyName: String,
    ): Optional<ByteArray>
    fun putObject(bucketName: String,
                  keyName: String,
                  obj: ByteArray)
}
