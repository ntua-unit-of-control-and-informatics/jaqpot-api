package org.jaqpot.api.storage.encoding

enum class Encoding {
    @Deprecated("Use RAW instead. Base64 files are 33% larger, so there's no reason to store files as base64. This will be removed in a future version.")
    BASE64,
    RAW
}
