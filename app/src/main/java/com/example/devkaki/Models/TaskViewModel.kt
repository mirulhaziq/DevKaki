package com.example.devkaki.viewmodel

import androidx.lifecycle.ViewModel
import com.example.devkaki.model.Task
import com.example.devkaki.model.Priority
import com.example.devkaki.model.TaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        // Add some sample tasks for testing
        _tasks.value = listOf(
            Task(
                name = "Fix login bug",
                description = "Users cannot log in with email",
                type = TaskType.BUG,
                priority = Priority.HIGH,
                dueDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // Tomorrow
                estimatedHours = 2
            ),
            Task(
                name = "Learn Jetpack Compose",
                description = "Complete Compose tutorial",
                type = TaskType.LEARNING,
                priority = Priority.MEDIUM,
                dueDate = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000), // 3 days
                estimatedHours = 8
            )
        )
    }

    fun addTask(task: Task) {
        _tasks.value = _tasks.value + task
    }

    fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map {
            if (it.id == task.id) task else it
        }
    }

    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(
                    isCompleted = !task.isCompleted,
                    completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
                )
            } else {
                task
            }
        }
    }

    fun getTaskById(taskId: String): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    fun getTodayTasks(): List<Task> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val tomorrow = today + (24 * 60 * 60 * 1000)

        return _tasks.value.filter { task ->
            task.dueDate != null && task.dueDate in today until tomorrow
        }
    }

    fun getUpcomingTasks(): List<Task> {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return _tasks.value.filter { task ->
            task.dueDate != null && task.dueDate >= tomorrow && !task.isCompleted
        }
    }

    fun getOverdueTasks(): List<Task> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return _tasks.value.filter { task ->
            task.dueDate != null && task.dueDate < today && !task.isCompleted
        }
    }

    fun getWeeklyTasks(): List<Task> {
        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val weekEnd = weekStart + (7 * 24 * 60 * 60 * 1000)

        return _tasks.value.filter { task ->
            task.dueDate != null && task.dueDate in weekStart until weekEnd
        }
    }

    fun getCompletedTasks(): List<Task> {
        return _tasks.value.filter { it.isCompleted }
    }

    fun getTasksByPriority(priority: Priority): List<Task> {
        return _tasks.value.filter { it.priority == priority }
    }

    fun getTasksByType(type: TaskType): List<Task> {
        return _tasks.value.filter { it.type == type }
    }
}