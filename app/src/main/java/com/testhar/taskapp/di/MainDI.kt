package com.testhar.taskapp.di

import android.content.Context
import androidx.room.Room
import com.testhar.taskapp.data.dbclient.dao.TaskDao
import com.testhar.taskapp.data.dbclient.db.TaskDatabase
import com.testhar.taskapp.domain.repository.TaskRepository
import com.testhar.taskapp.domain.repository.TaskRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainDI {

    /** Room database instance */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): TaskDatabase {
        return Room.databaseBuilder(
            appContext,
            TaskDatabase::class.java,
            "task_app_database"
        ).fallbackToDestructiveMigration(false)
            .build()
    }

    /** DA0 */
    @Provides
    fun provideTaskDao(db: TaskDatabase): TaskDao = db.taskDao()

    /** Repository */
    @Provides
    @Singleton
    fun provideTaskRepository(dao: TaskDao): TaskRepository = TaskRepositoryImpl(dao)
}