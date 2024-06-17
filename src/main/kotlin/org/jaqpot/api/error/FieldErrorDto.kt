package org.jaqpot.api.error

class FieldErrorDto(val objectName: String, val field: String, val code: String?, val defaultMessage: String?) {
    override fun toString(): String {
        return "Field $field validation error: $defaultMessage"
    }
}
