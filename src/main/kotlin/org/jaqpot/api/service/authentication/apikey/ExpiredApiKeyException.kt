package org.jaqpot.api.service.authentication.apikey

class ExpiredApiKeyException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
