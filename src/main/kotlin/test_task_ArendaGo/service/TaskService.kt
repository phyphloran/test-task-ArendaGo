package test_task_ArendaGo.service

import reactor.core.publisher.Mono
import test_task_ArendaGo.dto.CreateTaskRequest
import test_task_ArendaGo.dto.PageResponse
import test_task_ArendaGo.dto.TaskResponse
import test_task_ArendaGo.dto.UpdateTaskStatusRequest
import test_task_ArendaGo.model.TaskStatus

interface TaskService {
    fun createTask(request: CreateTaskRequest): Mono<TaskResponse>
    fun getTaskById(id: Long): Mono<TaskResponse>
    fun getTasks(page: Int, size: Int, status: TaskStatus?): Mono<PageResponse<TaskResponse>>
    fun updateStatus(id: Long, request: UpdateTaskStatusRequest): Mono<TaskResponse>
    fun deleteTask(id: Long): Mono<Void>
}
