package com.testhar.taskapp.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.testhar.taskapp.domain.repository.TaskRepository
import com.testhar.taskapp.utils.FilterState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


class TestableTaskViewModel(
    repository: TaskRepository,
    override val sharingStarted: SharingStarted = SharingStarted.Eagerly
) : TaskViewModel(repository) {

    override val filteredTasks = combine(allTasks, filter) { tasks, filter ->
        when (filter) {
            FilterState.All -> tasks
            FilterState.Completed -> tasks.filter { it.isCompleted }
            FilterState.Pending -> tasks.filter { !it.isCompleted }
        }
    }.stateIn(viewModelScope, sharingStarted, emptyList())
}