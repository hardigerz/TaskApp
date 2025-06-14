package com.testhar.taskapp

import androidx.lifecycle.viewModelScope
import com.testhar.taskapp.domain.model.Task
import com.testhar.taskapp.domain.repository.TaskRepository
import com.testhar.taskapp.ui.viewmodel.TaskViewModel
import com.testhar.taskapp.utils.FilterState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.testhar.taskapp.domain.repository.FakeTaskRepository
import com.testhar.taskapp.ui.viewmodel.TestableTaskViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private lateinit var viewModel: TaskViewModel
    private lateinit var repository: FakeTaskRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository()
        viewModel = TestableTaskViewModel(repository, SharingStarted.Eagerly)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addTask should insert a new task`() = runTest(testDispatcher) {
        viewModel.addTask("Test Title", "Test Description", false)
        advanceUntilIdle()

        val tasks = repository.getTasks()
        assertEquals(1, tasks.size)
        assertEquals("Test Title", tasks[0].title)
    }

    @Test
    fun `updateTask should modify existing task`() = runTest(testDispatcher) {
        val task = Task(id = 100, title = "Old", description = "Desc", isCompleted = false)
        repository.insertTask(task)
        advanceUntilIdle()

        viewModel.updateTask(task.copy(title = "Updated"))
        advanceUntilIdle()

        val tasks = repository.getTasks()
        println("🧪 Final tasks: $tasks")
        assertEquals("Updated", tasks[0].title)
    }

    @Test
    fun `deleteTask should remove task`() = runTest(testDispatcher) {
        val task = Task(id = 1, title = "To Delete", description = null, isCompleted = false)
        repository.insertTask(task)

        viewModel.deleteTask(task)
        advanceUntilIdle()

        val tasks = repository.getTasks()
        assertTrue(tasks.isEmpty())
    }

    @Test
    fun `filteredTasks emits only completed tasks when filter is Completed`() = runTest(testDispatcher) {
        // Arrange
        repository.insertTask(Task(id = 1, title = "Done 1", isCompleted = true))
        repository.insertTask(Task(id = 2, title = "Done 2", isCompleted = true))
        repository.insertTask(Task(id = 3, title = "Pending", isCompleted = false))

        val viewModel = TestableTaskViewModel(repository, SharingStarted.Eagerly)

        viewModel.setFilter(FilterState.Completed)
        advanceUntilIdle()

        val result = viewModel.filteredTasks.value

        println("✅ Emitted: $result")

        assertEquals(2, result.size)
        assertTrue(result.all { it.isCompleted })
    }
}