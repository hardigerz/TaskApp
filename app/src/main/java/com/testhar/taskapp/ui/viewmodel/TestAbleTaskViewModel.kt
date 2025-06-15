package com.testhar.taskapp.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.testhar.taskapp.domain.repository.TaskRepository
import com.testhar.taskapp.utils.TaskStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


class TestableTaskViewModel(
    repository: TaskRepository,
    override val sharingStarted: SharingStarted = SharingStarted.Eagerly
) : TaskViewModel(repository) {

    override val filteredTasks = combine(allTasks, filter) { tasks, filter ->
        when (filter) {
            TaskStatus.All -> tasks
            TaskStatus.Completed -> tasks.filter { it.isCompleted }
            TaskStatus.Pending -> tasks.filter { !it.isCompleted }
        }
    }.stateIn(viewModelScope, sharingStarted, emptyList())
}