package com.testhar.taskapp

import com.testhar.taskapp.domain.model.Task
import com.testhar.taskapp.ui.viewmodel.TaskViewModel
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
import com.testhar.taskapp.utils.TaskStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description


@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: TaskViewModel
    private lateinit var repository: FakeTaskRepository
    private val testDispatcher = StandardTestDispatcher()



    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository()
        viewModel = TestableTaskViewModel(repository, SharingStarted.Eagerly)
        // default filter = ALL, but let's be explicit:
        viewModel.setFilter(TaskStatus.All)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `deleteTask should remove task`() = runTest {
        // 1Bind Main to this test's scheduler
        val mainDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(mainDispatcher)

        viewModel = TaskViewModel(repository)
        viewModel.setFilter(TaskStatus.All)

        //Insert and then delete
        val task = Task(id = 1, title = "To Delete", description = null, isCompleted = false)
        repository.insertTask(task)
        viewModel.deleteTask(task)

        // Advance until everything queued on the test scheduler runs
        advanceUntilIdle()

        // Assert the in-memory repo is empty
        assertTrue(repository.getTasks().isEmpty())

        // Clean up
        Dispatchers.resetMain()
    }

    @Test
    fun `filteredTasks emits only completed tasks when filter is Completed`() = runTest(testDispatcher) {
        // Arrange
        repository.insertTask(Task(id = 1, title = "Done 1", isCompleted = true))
        repository.insertTask(Task(id = 2, title = "Done 2", isCompleted = true))
        repository.insertTask(Task(id = 3, title = "Pending", isCompleted = false))

        val viewModel = TestableTaskViewModel(repository, SharingStarted.Eagerly)

        viewModel.setFilter(TaskStatus.Completed)
        advanceUntilIdle()

        val result = viewModel.filteredTasks.value

        println("✅ Emitted: $result")

        assertEquals(2, result.size)
        assertTrue(result.all { it.isCompleted })
    }

    @Test
    fun `filteredTasks emits all tasks when filter is All`() = runTest(testDispatcher) {
        // Arrange
        val task1 = Task(id = 1, title = "Task 1", isCompleted = true)
        val task2 = Task(id = 2, title = "Task 2", isCompleted = false)
        val task3 = Task(id = 3, title = "Task 3", isCompleted = true)

        repository.insertTask(task1)
        repository.insertTask(task2)
        repository.insertTask(task3)

        val viewModel = TestableTaskViewModel(repository, SharingStarted.Eagerly)

        viewModel.setFilter(TaskStatus.All)
        advanceUntilIdle()

        // Assert
        val result = viewModel.filteredTasks.value
        println("✅ Emitted: $result")
        assertEquals(3, result.size)
        assertTrue(result.containsAll(listOf(task1, task2, task3)))
    }


    @Test
    fun `filteredTasks emits only pending tasks when filter is Pending`() = runTest(testDispatcher) {
        // Arrange
        val task1 = Task(id = 1, title = "Done 1", isCompleted = true)
        val task2 = Task(id = 2, title = "Pending 1", isCompleted = false)
        val task3 = Task(id = 3, title = "Pending 2", isCompleted = false)

        repository.insertTask(task1)
        repository.insertTask(task2)
        repository.insertTask(task3)

        val viewModel = TestableTaskViewModel(repository, SharingStarted.Eagerly)

        viewModel.setFilter(TaskStatus.Pending)
        advanceUntilIdle()

        // Assert
        val result = viewModel.filteredTasks.value
        println("✅ Emitted: $result")
        assertEquals(2, result.size)
        assertTrue(result.all { !it.isCompleted })
    }

    @Test
    fun `observeTasks emits inserted tasks`() = runTest(testDispatcher) {
        // Arrange
        val task1 = Task(id = 1, title = "One", isCompleted = false)
        val task2 = Task(id = 2, title = "Two", isCompleted = true)

        repository.insertTask(task1)
        repository.insertTask(task2)

        // Act
        val emitted = repository.observeTasks().first()

        // Assert
        assertEquals(2, emitted.size)
        assertTrue(emitted.containsAll(listOf(task1, task2)))
    }

    @Test
    fun `getTaskById returns correct task`() = runTest(testDispatcher) {
        // Arrange
        val task1 = Task(id = 101, title = "Alpha", isCompleted = false)
        val task2 = Task(id = 102, title = "Beta", isCompleted = true)
        repository.insertTask(task1)
        repository.insertTask(task2)

        val viewModel = TestableTaskViewModel(repository, SharingStarted.Eagerly)

        // Act
        val foundTask = viewModel.getTaskById(102)

        // Assert
        assertEquals("Beta", foundTask?.title)
        assertEquals(true, foundTask?.isCompleted)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    class MainDispatcherRule(
        val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }
        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}