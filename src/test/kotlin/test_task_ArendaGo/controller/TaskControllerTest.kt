package test_task_ArendaGo.controller

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import reactor.core.publisher.Mono
import test_task_ArendaGo.dto.PageResponse
import test_task_ArendaGo.dto.TaskResponse
import test_task_ArendaGo.exception.GlobalExceptionHandler
import test_task_ArendaGo.exception.TaskNotFoundException
import test_task_ArendaGo.model.TaskStatus
import test_task_ArendaGo.service.TaskService
import java.time.LocalDateTime

@WebMvcTest(TaskController::class)
@Import(GlobalExceptionHandler::class)
class TaskControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var taskService: TaskService

    @Test
    fun `POST api tasks should return 201`() {
        whenever(taskService.createTask(any())).thenReturn(Mono.just(taskResponse(1L, TaskStatus.NEW)))

        val result = mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "Prepare report",
                      "description": "Monthly financial report"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(request().asyncStarted())
            .andReturn()

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("NEW"))
    }

    @Test
    fun `POST api tasks should validate title`() {
        mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "ab",
                      "description": "Monthly financial report"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.validationErrors.title").exists())
    }

    @Test
    fun `GET api tasks by id should return 404 when task missing`() {
        whenever(taskService.getTaskById(404L)).thenReturn(Mono.error(TaskNotFoundException(404L)))

        val result = mockMvc.perform(get("/api/v1/tasks/404"))
            .andExpect(request().asyncStarted())
            .andReturn()

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Task with id=404 not found"))
    }

    @Test
    fun `GET api tasks should return paginated response`() {
        val response = PageResponse(
            content = listOf(taskResponse(1L, TaskStatus.NEW)),
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1
        )
        whenever(taskService.getTasks(0, 10, TaskStatus.NEW)).thenReturn(Mono.just(response))

        val result = mockMvc.perform(get("/api/v1/tasks?page=0&size=10&status=NEW"))
            .andExpect(request().asyncStarted())
            .andReturn()

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
    }

    @Test
    fun `GET api tasks should validate max size`() {
        mockMvc.perform(get("/api/v1/tasks?page=0&size=1000"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Request validation failed"))
    }

    @Test
    fun `PATCH api tasks status should return updated task`() {
        whenever(taskService.updateStatus(any(), any())).thenReturn(Mono.just(taskResponse(1L, TaskStatus.DONE)))

        val result = mockMvc.perform(
            patch("/api/v1/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"status":"DONE"}""")
        )
            .andExpect(request().asyncStarted())
            .andReturn()

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("DONE"))
    }

    @Test
    fun `DELETE api tasks should return 204`() {
        whenever(taskService.deleteTask(1L)).thenReturn(Mono.empty())

        val result = mockMvc.perform(delete("/api/v1/tasks/1"))
            .andExpect(request().asyncStarted())
            .andReturn()

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `DELETE api tasks should return 404 when task is missing`() {
        whenever(taskService.deleteTask(404L)).thenReturn(Mono.error(TaskNotFoundException(404L)))

        val result = mockMvc.perform(delete("/api/v1/tasks/404"))
            .andExpect(request().asyncStarted())
            .andReturn()

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Task with id=404 not found"))
    }

    private fun taskResponse(id: Long, status: TaskStatus): TaskResponse {
        val now = LocalDateTime.of(2026, 3, 26, 12, 0)
        return TaskResponse(
            id = id,
            title = "Prepare report",
            description = "Monthly financial report",
            status = status,
            createdAt = now,
            updatedAt = now
        )
    }
}
