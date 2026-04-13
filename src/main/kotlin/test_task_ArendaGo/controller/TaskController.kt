package test_task_ArendaGo.controller


import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import test_task_ArendaGo.dto.CreateTaskRequest
import test_task_ArendaGo.dto.PageResponse
import test_task_ArendaGo.dto.TaskResponse
import test_task_ArendaGo.dto.UpdateTaskStatusRequest
import test_task_ArendaGo.model.TaskStatus
import test_task_ArendaGo.service.TaskService


@RestController
@Validated
@RequestMapping("/api/v1/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    fun createTask(@Valid @RequestBody request: CreateTaskRequest): Mono<ResponseEntity<TaskResponse>> =
        taskService.createTask(request)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    fun getTasks(
        @RequestParam @Min(value = 0, message = "page must be >= 0") page: Int,
        @RequestParam
        @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100")
        size: Int,
        @RequestParam(required = false) status: TaskStatus?
    ): Mono<PageResponse<TaskResponse>> = taskService.getTasks(page, size, status)

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: Long): Mono<TaskResponse> = taskService.getTaskById(id)

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTaskStatusRequest
    ): Mono<TaskResponse> = taskService.updateStatus(id, request)

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): Mono<ResponseEntity<Void>> =
        taskService.deleteTask(id).thenReturn(ResponseEntity.noContent().build())
}
