package com.testhar.taskapp.data.dbclient.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.testhar.taskapp.data.dbclient.TaskEntity
import com.testhar.taskapp.data.dbclient.dao.TaskDao

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}