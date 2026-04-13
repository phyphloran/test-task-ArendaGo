package test_task_ArendaGo.mapper

import org.springframework.stereotype.Component
import test_task_ArendaGo.dto.PageResponse
import test_task_ArendaGo.dto.TaskResponse
import test_task_ArendaGo.model.Task

@Component
class TaskMapper {

    fun toResponse(task: Task): TaskResponse =
        TaskResponse(
            id = task.id,
            title = task.title,
            description = task.description,
            status = task.status,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt
        )

    fun toPageResponse(
        tasks: List<Task>,
        page: Int,
        size: Int,
        totalElements: Long,
        totalPages: Int
    ): PageResponse<TaskResponse> =
        PageResponse(
            content = tasks.map { toResponse(it) },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages
        )
}
