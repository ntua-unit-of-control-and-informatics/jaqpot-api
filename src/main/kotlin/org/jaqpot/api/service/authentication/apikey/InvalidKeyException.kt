package org.jaqpot.api.service.authentication.apikey

class InvalidKeyException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
