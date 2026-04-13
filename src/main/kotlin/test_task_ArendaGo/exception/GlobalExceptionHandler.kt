package test_task_ArendaGo.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.validation.method.ParameterValidationResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.slf4j.LoggerFactory
import test_task_ArendaGo.dto.ErrorResponse
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(TaskNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTaskNotFound(
        ex: TaskNotFoundException,
        request: HttpServletRequest
    ): ErrorResponse = errorResponse(
        status = HttpStatus.NOT_FOUND,
        message = ex.message ?: "Task not found",
        path = request.requestURI
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationError(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ErrorResponse {
        val fieldErrors: Map<String, String> = ex.bindingResult.fieldErrors.associate { error: FieldError ->
            error.field to (error.defaultMessage ?: "Invalid value")
        }

        return errorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Request validation failed",
            path = request.requestURI,
            validationErrors = fieldErrors
        )
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodValidationError(
        ex: HandlerMethodValidationException,
        request: HttpServletRequest
    ): ErrorResponse {
        val validationErrors: Map<String, String> = ex.parameterValidationResults
            .flatMap { validationResult: ParameterValidationResult ->
                val parameterName = validationResult.methodParameter.parameterName ?: "parameter"
                validationResult.resolvableErrors.map { error ->
                    parameterName to (error.defaultMessage ?: "Invalid value")
                }
            }
            .toMap()

        return errorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Request validation failed",
            path = request.requestURI,
            validationErrors = validationErrors
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ErrorResponse {
        val validationErrors: Map<String, String> = ex.constraintViolations.associate { violation: ConstraintViolation<*> ->
            violation.propertyPath.toString().substringAfterLast(".") to violation.message
        }

        return errorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Request validation failed",
            path = request.requestURI,
            validationErrors = validationErrors
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class, HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleTypeMismatch(
        ex: Exception,
        request: HttpServletRequest
    ): ErrorResponse = errorResponse(
        status = HttpStatus.BAD_REQUEST,
        message = "Invalid request format",
        path = request.requestURI
    )

    @ExceptionHandler(NoResourceFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoResourceFound(
        ex: NoResourceFoundException,
        request: HttpServletRequest
    ): ErrorResponse = errorResponse(
        status = HttpStatus.NOT_FOUND,
        message = ex.message ?: "Resource not found",
        path = request.requestURI
    )

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpectedError(
        ex: Exception,
        request: HttpServletRequest
    ): ErrorResponse {
        log.error("Unhandled exception on {} {}", request.method, request.requestURI, ex)

        return errorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "Unexpected error occurred. Please try again later.",
            path = request.requestURI
        )
    }

    private fun errorResponse(
        status: HttpStatus,
        message: String,
        path: String,
        validationErrors: Map<String, String> = emptyMap()
    ): ErrorResponse =
        ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            message = message,
            path = path,
            validationErrors = validationErrors
        )
}
