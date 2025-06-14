
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
) : ViewModel() {

    protected open val sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5000)

    private val _filter = MutableStateFlow(FilterState.All)
    val filter = _filter.asStateFlow()

    protected open val allTasks: Flow<List<Task>> = repository
        .observeTasks()
        .distinctUntilChanged()

    open val filteredTasks: StateFlow<List<Task>> by lazy {
        combine(allTasks, _filter) { tasks, filter ->
            when (filter) {
                FilterState.All -> tasks
                FilterState.Completed -> tasks.filter { it.isCompleted }
                FilterState.Pending -> tasks.filter { !it.isCompleted }
            }
        }.stateIn(viewModelScope, sharingStarted, emptyList())
    }

    //    // Get single task by ID (used for editing)
    suspend fun getTaskById(id: Int): Task? = repository.observeTasks()
        .first().find { it.id == id }

    fun addTask(title: String, description: String?, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.insertTask(Task(title = title, description = description, isCompleted = isCompleted))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun setFilter(state: FilterState) {
        _filter.value = state
    }
}
