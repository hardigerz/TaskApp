
package com.testhar.taskapp.domain.repository

import com.testhar.taskapp.data.dbclient.TaskEntity
import com.testhar.taskapp.data.dbclient.dao.TaskDao
import com.testhar.taskapp.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        dao.observeAllTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun insertTask(task: Task) {
        dao.insert(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        dao.update(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        dao.delete(task.toEntity())
    }

    // Mappers
    private fun TaskEntity.toDomain() = Task(id, title, description, isCompleted)
    private fun Task.toEntity() = TaskEntity(id, title, description, isCompleted)
}