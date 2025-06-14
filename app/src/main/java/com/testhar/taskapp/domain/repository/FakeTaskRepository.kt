package com.testhar.taskapp.domain.repository

import android.os.Build
import com.testhar.taskapp.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class FakeTaskRepository : TaskRepository {
    private val taskList = mutableListOf<Task>()
    private val taskFlow = MutableStateFlow<List<Task>>(emptyList())

    private var nextId = 1

    override suspend fun insertTask(task: Task) {
        val taskWithId = if (task.id == 0) task.copy(id = nextId++) else task
        taskList.add(taskWithId)
        taskFlow.value = taskList.toList()
    }

    override suspend fun updateTask(task: Task) {
        val index = taskList.indexOfFirst { it.id == task.id }
        if (index != -1) {
            taskList[index] = task
            taskFlow.value = taskList.toList()
        }
    }

    override suspend fun deleteTask(task: Task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            taskList.removeIf { it.id == task.id }
        }
        taskFlow.value = taskList.toList()
    }

    override fun observeTasks(): Flow<List<Task>> = taskFlow.asStateFlow()

    fun getTasks(): List<Task> = taskList.toList()
}