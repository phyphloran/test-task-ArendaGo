package test_task_ArendaGo.dto

import java.time.LocalDateTime
import java.util.Collections.emptyMap

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val validationErrors: Map<String, String> = emptyMap()
)
