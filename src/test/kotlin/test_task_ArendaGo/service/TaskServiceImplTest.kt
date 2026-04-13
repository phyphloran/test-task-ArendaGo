package test_task_ArendaGo.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.test.StepVerifier
import test_task_ArendaGo.dto.CreateTaskRequest
import test_task_ArendaGo.dto.UpdateTaskStatusRequest
import test_task_ArendaGo.exception.TaskNotFoundException
import test_task_ArendaGo.model.Task
import test_task_ArendaGo.model.TaskStatus
import test_task_ArendaGo.repository.TaskRepository
import java.time.LocalDateTime

class TaskServiceImplTest {

    private val taskRepository: TaskRepository = mock()
    private val taskService: TaskService = TaskServiceImpl(taskRepository)

    @Test
    fun `createTask should create task successfully`() {
        val task = task(id = 1L, status = TaskStatus.NEW)
        whenever(taskRepository.save("Prepare report", "Monthly financial report")).thenReturn(task)

        StepVerifier.create(
            taskService.createTask(
                CreateTaskRequest(
                    title = "Prepare report",
                    description = "Monthly financial report"
                )
            )
        )
            .assertNext { response ->
                assertThat(response.id).isEqualTo(1L)
                assertThat(response.status).isEqualTo(TaskStatus.NEW)
                assertThat(response.title).isEqualTo("Prepare report")
            }
            .verifyComplete()
    }

    @Test
    fun `getTaskById should return task when exists`() {
        whenever(taskRepository.findById(1L)).thenReturn(task(id = 1L, status = TaskStatus.IN_PROGRESS))

        StepVerifier.create(taskService.getTaskById(1L))
            .assertNext { response ->
                assertThat(response.id).isEqualTo(1L)
                assertThat(response.status).isEqualTo(TaskStatus.IN_PROGRESS)
            }
            .verifyComplete()
    }

    @Test
    fun `getTaskById should return error when task is missing`() {
        whenever(taskRepository.findById(42L)).thenReturn(null)

        StepVerifier.create(taskService.getTaskById(42L))
            .expectErrorSatisfies { error ->
                assertThat(error).isInstanceOf(TaskNotFoundException::class.java)
                assertThat(error.message).isEqualTo("Task with id=42 not found")
            }
            .verify()
    }

    @Test
    fun `updateStatus should update task status`() {
        whenever(taskRepository.updateStatus(1L, TaskStatus.DONE)).thenReturn(task(id = 1L, status = TaskStatus.DONE))

        StepVerifier.create(taskService.updateStatus(1L, UpdateTaskStatusRequest(TaskStatus.DONE)))
            .assertNext { response ->
                assertThat(response.id).isEqualTo(1L)
                assertThat(response.status).isEqualTo(TaskStatus.DONE)
            }
            .verifyComplete()
    }

    @Test
    fun `deleteTask should complete successfully`() {
        whenever(taskRepository.deleteById(1L)).thenReturn(true)

        StepVerifier.create(taskService.deleteTask(1L))
            .verifyComplete()

        verify(taskRepository).deleteById(1L)
    }

    @Test
    fun `getTasks should return paged result with filter`() {
        val tasks = listOf(
            task(id = 10L, status = TaskStatus.NEW),
            task(id = 9L, status = TaskStatus.NEW)
        )
        whenever(taskRepository.findAll(0, 2, TaskStatus.NEW)).thenReturn(tasks)
        whenever(taskRepository.countAll(TaskStatus.NEW)).thenReturn(5L)

        StepVerifier.create(taskService.getTasks(page = 0, size = 2, status = TaskStatus.NEW))
            .assertNext { response ->
                assertThat(response.content).hasSize(2)
                assertThat(response.totalElements).isEqualTo(5L)
                assertThat(response.totalPages).isEqualTo(3)
                assertThat(response.page).isEqualTo(0)
                assertThat(response.size).isEqualTo(2)
            }
            .verifyComplete()
    }

    private fun task(id: Long, status: TaskStatus): Task {
        val now = LocalDateTime.of(2026, 3, 26, 12, 0)
        return Task(
            id = id,
            title = "Prepare report",
            description = "Monthly financial report",
            status = status,
            createdAt = now,
            updatedAt = now
        )
    }
}
