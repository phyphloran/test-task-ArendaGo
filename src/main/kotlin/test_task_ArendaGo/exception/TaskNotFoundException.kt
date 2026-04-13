package test_task_ArendaGo.exception

class TaskNotFoundException(id: Long) : RuntimeException("Task with id=$id not found")
