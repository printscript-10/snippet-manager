package org.prinstcript10.snippetmanager.shared.exception

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.HttpClientErrorException

@ControllerAdvice
class ExceptionHandler {
    private val objectMapper: ObjectMapper = ObjectMapper()

    @ExceptionHandler(HttpException::class)
    fun handleHttpException(ex: HttpException): ResponseEntity<Any> {
        val errorDetails =
            mapOf(
                "timestamp" to System.currentTimeMillis(),
                "status" to ex.status.value(),
                "error" to ex.status.reasonPhrase,
                "message" to ex.message,
            )
        return ResponseEntity(errorDetails, ex.status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<Any> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.fieldErrors.forEach { error ->
            val fieldName = error.field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            errors[fieldName] = errorMessage
        }
        val errorDetails =
            mapOf(
                "timestamp" to System.currentTimeMillis(),
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to HttpStatus.BAD_REQUEST.reasonPhrase,
                "message" to "Validation failed",
                "errors" to errors,
            )
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpClientErrorException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleHttpClientErrorException(ex: HttpClientErrorException): ResponseEntity<Any> {
        val errorMessage = try {
            val errorMap = objectMapper.readValue(ex.responseBodyAsString, Map::class.java)
            errorMap["message"]?.toString() ?: "Unexpected error"
        } catch (parseException: Exception) {
            ex.message ?: "Unexpected error"
        }

        val errorDetails =
            mapOf(
                "timestamp" to System.currentTimeMillis(),
                "status" to HttpStatus.CONFLICT.value(),
                "error" to HttpStatus.CONFLICT.reasonPhrase,
                "message" to errorMessage,
            )

        return ResponseEntity(errorDetails, HttpStatus.CONFLICT)
    }

}
