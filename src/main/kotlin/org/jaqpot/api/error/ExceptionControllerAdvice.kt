package org.jaqpot.api.error

import jakarta.ws.rs.BadRequestException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class ExceptionControllerAdvice {
    @ExceptionHandler
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ApiErrorResponse> {

        val errorMessage = ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.message
        )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleNotFoundException(ex: JaqpotNotFoundException): ResponseEntity<ApiErrorResponse> {
        val errorMessage = ApiErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.message
        )
        return ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler
    fun handleBadRequestException(ex: BadRequestException): ResponseEntity<ApiErrorResponse> {
        val errorMessage = ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.message
        )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val result = ex.bindingResult
        val fieldErrors: List<FieldErrorDto> = result.fieldErrors
            .map { f: FieldError ->
                FieldErrorDto(
                    f.objectName,
                    f.field,
                    f.code,
                    f.defaultMessage
                )
            }


        val errorMessage = ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            fieldErrors.joinToString { it.toString() }
        )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }
}
