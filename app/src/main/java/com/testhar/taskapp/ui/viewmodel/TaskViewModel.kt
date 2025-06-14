
package com.testhar.taskapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testhar.taskapp.domain.model.Task
import com.testhar.taskapp.domain.repository.TaskRepository
import com.testhar.taskapp.utils.FilterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    // Holds current filter (All, Completed, Pending)
    private val _filter = MutableStateFlow(FilterState.All)
    val filter = _filter.asStateFlow()

    // Main source of truth: observe from Room using Flow
    private val allTasks: Flow<List<Task>> = repository
        .observeTasks()
        .distinctUntilChanged() // Avoid reprocessing same list
        .onEach { Log.d("ViewModel", "📡 observed ${it.size} tasks from DB") }

    // Combine tasks + filter to produce filtered output
    val filteredTasks: StateFlow<List<Task>> = combine(allTasks, _filter) { tasks, filter ->
        when (filter) {
            FilterState.All -> tasks
            FilterState.Completed -> tasks.filter { it.isCompleted }
            FilterState.Pending -> tasks.filter { !it.isCompleted }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )


    // Get single task by ID (used for editing)
    suspend fun getTaskById(id: Int): Task? = repository.observeTasks()
        .first().find { it.id == id }

    // insertTask
    fun addTask(title: String, description: String?, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.insertTask(Task(title = title, description = description,  isCompleted = isCompleted))
        }
    }

    // update task
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    // delete task
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // filtering set
    fun setFilter(state: FilterState) {
        _filter.value = state
    }
}