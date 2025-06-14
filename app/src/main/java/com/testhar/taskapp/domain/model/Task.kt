package com.testhar.taskapp.domain.model
data class Task(
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false
)
