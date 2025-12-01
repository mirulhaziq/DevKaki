// AllTasksScreen.kt
package com.example.devkaki.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.devkaki.model.*
import com.example.devkaki.viewmodel.TaskViewModel
import com.example.devkaki.ui.theme.DevKakiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasksScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    var filterStatus by remember { mutableStateOf<String?>("All") }
    var filterPriority by remember { mutableStateOf<Priority?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val filteredTasks = tasks.filter { task ->
        val statusMatch = when (filterStatus) {
            "Completed" -> task.isCompleted
            "Pending" -> !task.isCompleted
            else -> true
        }
        val priorityMatch = filterPriority == null || task.priority == filterPriority
        statusMatch && priorityMatch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Tasks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "${filteredTasks.size} task(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            if (filteredTasks.isEmpty()) {
                item {
                    EmptyState("No tasks found")
                }
            } else {
                items(filteredTasks) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task.id) },
                        onClick = { onNavigateToDetail(task.id) }
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Tasks") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Status", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filterStatus == "All",
                            onClick = { filterStatus = "All" },
                            label = { Text("All") }
                        )
                        FilterChip(
                            selected = filterStatus == "Completed",
                            onClick = { filterStatus = "Completed" },
                            label = { Text("Completed") }
                        )
                        FilterChip(
                            selected = filterStatus == "Pending",
                            onClick = { filterStatus = "Pending" },
                            label = { Text("Pending") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Priority", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filterPriority == null,
                            onClick = { filterPriority = null },
                            label = { Text("All") }
                        )
                        Priority.values().forEach { priority ->
                            FilterChip(
                                selected = filterPriority == priority,
                                onClick = { filterPriority = priority },
                                label = { Text(priority.name) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AllTasksScreenPreview() {
    DevKakiTheme {
        AllTasksScreen(
            viewModel = TaskViewModel(),
            onNavigateBack = {},
            onNavigateToDetail = {}
        )
    }
}