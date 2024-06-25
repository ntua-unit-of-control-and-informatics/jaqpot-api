package org.jaqpot.api.error

class ApiErrorResponse(
    var status: Int? = null,
    var message: String? = null,
    var code: Int? = null
)
