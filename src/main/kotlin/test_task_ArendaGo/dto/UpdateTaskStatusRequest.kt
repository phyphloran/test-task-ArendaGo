package test_task_ArendaGo.dto

import jakarta.validation.constraints.NotNull
import test_task_ArendaGo.model.TaskStatus

data class UpdateTaskStatusRequest(
    @field:NotNull(message = "status is required")
    val status: TaskStatus?
)
