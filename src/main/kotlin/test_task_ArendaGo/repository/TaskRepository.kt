package test_task_ArendaGo.repository

import test_task_ArendaGo.model.Task
import test_task_ArendaGo.model.TaskStatus

interface TaskRepository {
    fun save(title: String, description: String?): Task
    fun findById(id: Long): Task?
    fun findAll(page: Int, size: Int, status: TaskStatus?): List<Task>
    fun countAll(status: TaskStatus?): Long
    fun updateStatus(id: Long, status: TaskStatus): Task?
    fun deleteById(id: Long): Boolean
}
