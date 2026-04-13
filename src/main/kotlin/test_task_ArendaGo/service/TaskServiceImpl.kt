package test_task_ArendaGo.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import test_task_ArendaGo.dto.CreateTaskRequest
import test_task_ArendaGo.dto.PageResponse
import test_task_ArendaGo.dto.TaskResponse
import test_task_ArendaGo.dto.UpdateTaskStatusRequest
import test_task_ArendaGo.exception.TaskNotFoundException
import test_task_ArendaGo.model.Task
import test_task_ArendaGo.model.TaskStatus
import test_task_ArendaGo.repository.TaskRepository
import kotlin.math.ceil

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository
) : TaskService {

    override fun createTask(request: CreateTaskRequest): Mono<TaskResponse> =
        Mono.fromCallable {
            val saved = taskRepository.save(
                title = request.title,
                description = request.description?.trim()?.ifBlank { null }
            )
            saved.toResponse()
        }.subscribeOn(Schedulers.boundedElastic())

    override fun getTaskById(id: Long): Mono<TaskResponse> =
        Mono.fromCallable {
            val task = taskRepository.findById(id) ?: throw TaskNotFoundException(id)
            task.toResponse()
        }.subscribeOn(Schedulers.boundedElastic())

    override fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>> =
        Mono.fromCallable {
            val tasks = taskRepository.findAll(page, size, status)
            val totalElements = taskRepository.countAll(status)
            val totalPages = if (totalElements == 0L) 0 else ceil(totalElements.toDouble() / size).toInt()

            PageResponse(
                content = tasks.map { it.toResponse() },
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages
            )
        }.subscribeOn(Schedulers.boundedElastic())

    override fun updateStatus(id: Long, request: UpdateTaskStatusRequest): Mono<TaskResponse> =
        Mono.fromCallable {
            val status = requireNotNull(request.status) { "status is required" }
            val updated = taskRepository.updateStatus(id, status) ?: throw TaskNotFoundException(id)
            updated.toResponse()
        }.subscribeOn(Schedulers.boundedElastic())

    override fun deleteTask(id: Long): Mono<Void> =
        Mono.fromCallable {
            taskRepository.deleteById(id)
        }.subscribeOn(Schedulers.boundedElastic()).then()

    private fun Task.toResponse(): TaskResponse =
        TaskResponse(
            id = id,
            title = title,
            description = description,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}
