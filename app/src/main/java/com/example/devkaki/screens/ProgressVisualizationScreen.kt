// ProgressVisualizationScreen.kt
package com.example.devkaki.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devkaki.model.*
import com.example.devkaki.viewmodel.TaskViewModel
import com.example.devkaki.ui.theme.DevKakiTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressVisualizationScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val completedTasks = tasks.filter { it.isCompleted }
    val totalCompleted = completedTasks.size

    // Group tasks by type
    val tasksByType = tasks.groupBy { it.type }
        .mapValues { it.value.size }

    // Group tasks by priority
    val tasksByPriority = tasks.groupBy { it.priority }
        .mapValues { it.value.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Visualization") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Streak Card
            StreakCard(streak = calculateStreak(completedTasks))

            // Stats Grid
            StatsGrid(
                totalCompleted = totalCompleted,
                totalTasks = tasks.size
            )

            // Weekly Sprint Progress
            WeeklySprint(viewModel = viewModel)

            // Tasks by Type
            TasksByTypeChart(tasksByType = tasksByType)

            // Tasks by Priority
            TasksByPriorityChart(tasksByPriority = tasksByPriority)
        }
    }
}

// Helper function to calculate streak
fun calculateStreak(completedTasks: List<Task>): Int {
    if (completedTasks.isEmpty()) return 0

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    var streak = 0
    var currentDay = today.clone() as Calendar

    // Check last 30 days for consecutive completion
    for (i in 0 until 30) {
        val dayStart = currentDay.timeInMillis
        val dayEnd = dayStart + (24 * 60 * 60 * 1000)

        val hasCompletedTask = completedTasks.any { task ->
            task.completedAt != null && task.completedAt in dayStart until dayEnd
        }

        if (hasCompletedTask) {
            streak++
        } else if (i > 0) {
            // Break streak if no task completed (but allow for today)
            break
        }

        currentDay.add(Calendar.DAY_OF_YEAR, -1)
    }

    return streak
}

@Composable
fun StreakCard(streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🔥 Current Streak",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (streak > 0) "Keep it going!" else "Start today!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "$streak",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StatsGrid(totalCompleted: Int, totalTasks: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalCompleted",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Completed",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        Card(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalTasks",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Total Tasks",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun WeeklySprint(viewModel: TaskViewModel) {
    val weeklyTasks = viewModel.getWeeklyTasks()
    val completed = weeklyTasks.count { it.isCompleted }
    val total = weeklyTasks.size
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Weekly Sprint Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Progress Ring
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completed/$total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun TasksByTypeChart(tasksByType: Map<TaskType, Int>) {
    if (tasksByType.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tasks by Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val maxCount = tasksByType.values.maxOrNull() ?: 1

            tasksByType.forEach { (type, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = type.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((count.toFloat() / maxCount))
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TasksByPriorityChart(tasksByPriority: Map<Priority, Int>) {
    if (tasksByPriority.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tasks by Priority",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val maxCount = tasksByPriority.values.maxOrNull() ?: 1

            Priority.entries.forEach { priority ->
                val count = tasksByPriority[priority] ?: 0
                val color = when (priority) {
                    Priority.HIGH -> Color(0xFFEF4444)
                    Priority.MEDIUM -> Color(0xFFA78BFA)
                    Priority.LOW -> Color(0xFF22C55E)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = priority.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((count.toFloat() / maxCount))
                                .background(color, RoundedCornerShape(4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}

// Preview removed as ViewModels cannot be instantiated in previews