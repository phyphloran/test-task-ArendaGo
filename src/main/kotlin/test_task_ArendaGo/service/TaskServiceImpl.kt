package test_task_ArendaGo.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import test_task_ArendaGo.dto.CreateTaskRequest
import test_task_ArendaGo.dto.PageResponse
import test_task_ArendaGo.dto.TaskResponse
import test_task_ArendaGo.dto.UpdateTaskStatusRequest
import test_task_ArendaGo.exception.TaskNotFoundException
import test_task_ArendaGo.mapper.TaskMapper
import test_task_ArendaGo.model.TaskStatus
import test_task_ArendaGo.repository.TaskRepository
import kotlin.math.ceil

@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper,
    private val blockingScheduler: Scheduler = Schedulers.boundedElastic()
) : TaskService {

    override fun createTask(request: CreateTaskRequest): Mono<TaskResponse> =
        Mono.fromCallable {
            val saved = taskRepository.save(
                title = request.title,
                description = request.description?.trim()?.ifBlank { null }
            )
            taskMapper.toResponse(saved)
        }.subscribeOn(blockingScheduler)

    override fun getTaskById(id: Long): Mono<TaskResponse> =
        Mono.fromCallable {
            val task = taskRepository.findById(id) ?: throw TaskNotFoundException(id)
            taskMapper.toResponse(task)
        }.subscribeOn(blockingScheduler)

    override fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>> =
        Mono.fromCallable {
            val tasks = taskRepository.findAll(page, size, status)
            val totalElements = taskRepository.countAll(status)
            val totalPages = if (totalElements == 0L) 0 else ceil(totalElements.toDouble() / size).toInt()

            taskMapper.toPageResponse(
                tasks = tasks,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages
            )
        }.subscribeOn(blockingScheduler)

    override fun updateStatus(id: Long, request: UpdateTaskStatusRequest): Mono<TaskResponse> =
        Mono.fromCallable {
            val status = requireNotNull(request.status) { "status is required" }
            val updated = taskRepository.updateStatus(id, status) ?: throw TaskNotFoundException(id)
            taskMapper.toResponse(updated)
        }.subscribeOn(blockingScheduler)

    override fun deleteTask(id: Long): Mono<Void> =
        Mono.fromCallable {
            val deleted = taskRepository.deleteById(id)
            if (!deleted) {
                throw TaskNotFoundException(id)
            }
        }.subscribeOn(blockingScheduler).then()
}
