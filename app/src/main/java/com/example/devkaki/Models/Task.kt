package com.example.devkaki.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val type: TaskType,
    val priority: Priority,
    val dueDate: Long? = null,
    val estimatedHours: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val tags: List<String> = emptyList()
)

enum class TaskType {
    BUG,
    FEATURE,
    LEARNING,
    REFACTOR,
    MEETING,
    REVIEW
}

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}