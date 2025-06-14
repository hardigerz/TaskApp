
package com.testhar.taskapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.testhar.taskapp.data.dbclient.TaskEntity
import com.testhar.taskapp.data.dbclient.dao.TaskDao
import com.testhar.taskapp.data.dbclient.db.TaskDatabase
import org.junit.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: TaskDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java)
            .allowMainThreadQueries() // for testing only
            .build()
        dao = database.taskDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createTask(id: Int, title: String, completed: Boolean) = TaskEntity(
        id = id,
        title = title,
        description = null,
        isCompleted = completed
    )

    @Test
    fun insert_and_get_tasks() = runTest {
        val task = createTask(1, "Sample Task", false)
        dao.insert(task)

        val result = dao.observeAllTasks().first()
        assertEquals(1, result.size)
        assertEquals("Sample Task", result[0].title)
    }

    @Test
    fun update_task_successfully() = runTest {
        val task = createTask(2, "Old", false)
        dao.insert(task)

        val updated = task.copy(title = "Updated")
        dao.update(updated)

        val result = dao.observeAllTasks().first()
        assertEquals("Updated", result[0].title)
    }

    @Test
    fun delete_task_successfully() = runTest {
        val task = createTask(3, "To Delete", false)
        dao.insert(task)
        dao.delete(task)

        val result = dao.observeAllTasks().first()
        assertTrue(result.isEmpty())
    }
}