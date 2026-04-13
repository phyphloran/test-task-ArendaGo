package test_task_ArendaGo.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import test_task_ArendaGo.model.TaskStatus

@SpringBootTest
class JdbcTaskRepositoryIntegrationTest {

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var jdbcClient: JdbcClient

    @BeforeEach
    fun cleanTable() {
        jdbcClient.sql("DELETE FROM tasks").update()
    }

    @Test
    fun `save and findById should persist task`() {
        val saved = taskRepository.save("Prepare report", "Monthly financial report")
        val found = taskRepository.findById(saved.id)

        assertThat(found).isNotNull
        assertThat(found!!.id).isEqualTo(saved.id)
        assertThat(found.status).isEqualTo(TaskStatus.NEW)
        assertThat(found.title).isEqualTo("Prepare report")
    }

    @Test
    fun `findAll and countAll should respect status filter and pagination`() {
        taskRepository.save("Task 1", "A")
        taskRepository.save("Task 2", "B")
        val doneTask = taskRepository.save("Task 3", "C")
        taskRepository.updateStatus(doneTask.id, TaskStatus.DONE)

        val newTasks = taskRepository.findAll(page = 0, size = 10, status = TaskStatus.NEW)
        val totalDone = taskRepository.countAll(TaskStatus.DONE)

        assertThat(newTasks).allMatch { it.status == TaskStatus.NEW }
        assertThat(newTasks).hasSize(2)
        assertThat(totalDone).isEqualTo(1)
    }

    @Test
    fun `updateStatus should update status and updatedAt`() {
        val saved = taskRepository.save("Task to update", null)
        val beforeUpdate = taskRepository.findById(saved.id)!!

        val updated = taskRepository.updateStatus(saved.id, TaskStatus.IN_PROGRESS)

        assertThat(updated).isNotNull
        assertThat(updated!!.status).isEqualTo(TaskStatus.IN_PROGRESS)
        assertThat(updated.updatedAt).isAfterOrEqualTo(beforeUpdate.updatedAt)
    }

    @Test
    fun `deleteById should remove task`() {
        val saved = taskRepository.save("Task to delete", null)

        val deleted = taskRepository.deleteById(saved.id)
        val found = taskRepository.findById(saved.id)

        assertThat(deleted).isTrue()
        assertThat(found).isNull()
    }

    @Test
    fun `deleteById should return false for missing task`() {
        val deleted = taskRepository.deleteById(99999L)

        assertThat(deleted).isFalse()
    }
}
