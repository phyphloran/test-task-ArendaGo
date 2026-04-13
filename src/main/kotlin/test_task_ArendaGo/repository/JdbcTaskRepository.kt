package test_task_ArendaGo.repository

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import test_task_ArendaGo.model.Task
import test_task_ArendaGo.model.TaskStatus
import java.time.LocalDateTime

@Repository
@Suppress("SqlNoDataSourceInspection", "SqlResolve")
class JdbcTaskRepository(
    private val jdbcClient: JdbcClient
) : TaskRepository {

    private val rowMapper = RowMapper<Task> { rs, _ ->
        Task(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            description = rs.getString("description"),
            status = TaskStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }

    @Transactional
    override fun save(title: String, description: String?): Task {
        val now = LocalDateTime.now()
        val keyHolder = GeneratedKeyHolder()

        jdbcClient.sql(
            """
            INSERT INTO tasks (title, description, status, created_at, updated_at)
            VALUES (:title, :description, :status, :createdAt, :updatedAt)
            """.trimIndent()
        )
            .param("title", title)
            .param("description", description)
            .param("status", TaskStatus.NEW.name)
            .param("createdAt", now)
            .param("updatedAt", now)
            .update(keyHolder, "id")

        val id = keyHolder.key?.toLong()
            ?: throw IllegalStateException("Failed to create task: no generated id")

        return findById(id) ?: throw IllegalStateException("Created task with id=$id was not found")
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): Task? =
        jdbcClient.sql(
            """
            SELECT id, title, description, status, created_at, updated_at
            FROM tasks
            WHERE id = :id
            """.trimIndent()
        )
            .param("id", id)
            .query(rowMapper)
            .optional()
            .orElse(null)

    @Transactional(readOnly = true)
    override fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task> {
        val offset = page * size

        return if (status == null) {
            jdbcClient.sql(
                """
                SELECT id, title, description, status, created_at, updated_at
                FROM tasks
                ORDER BY created_at DESC
                LIMIT :size OFFSET :offset
                """.trimIndent()
            )
                .param("size", size)
                .param("offset", offset)
                .query(rowMapper)
                .list()
        } else {
            jdbcClient.sql(
                """
                SELECT id, title, description, status, created_at, updated_at
                FROM tasks
                WHERE status = :status
                ORDER BY created_at DESC
                LIMIT :size OFFSET :offset
                """.trimIndent()
            )
                .param("status", status.name)
                .param("size", size)
                .param("offset", offset)
                .query(rowMapper)
                .list()
        }
    }

    @Transactional(readOnly = true)
    override fun countAll(status: TaskStatus?): Long =
        if (status == null) {
            jdbcClient.sql("SELECT COUNT(*) FROM tasks")
                .query(Long::class.java)
                .single()
        } else {
            jdbcClient.sql("SELECT COUNT(*) FROM tasks WHERE status = :status")
                .param("status", status.name)
                .query(Long::class.java)
                .single()
        }

    @Transactional
    override fun updateStatus(id: Long, status: TaskStatus): Task? {
        val affected = jdbcClient.sql(
            """
            UPDATE tasks
            SET status = :status,
                updated_at = :updatedAt
            WHERE id = :id
            """.trimIndent()
        )
            .param("id", id)
            .param("status", status.name)
            .param("updatedAt", LocalDateTime.now())
            .update()

        return if (affected == 0) null else findById(id)
    }

    @Transactional
    override fun deleteById(id: Long): Boolean =
        jdbcClient.sql("DELETE FROM tasks WHERE id = :id")
            .param("id", id)
            .update() > 0
}
