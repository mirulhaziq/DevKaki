package com.example.devkaki.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.devkaki.model.*
import com.example.devkaki.viewmodel.TaskViewModel
import com.example.devkaki.ui.theme.DevKakiTheme
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    taskId: String?
) {
    val existingTask: Task? = taskId?.let { viewModel.getTaskById(it) }

    var taskName by remember { mutableStateOf(existingTask?.name ?: "") }
    var selectedType by remember { mutableStateOf(existingTask?.type ?: TaskType.FEATURE) }
    var selectedPriority by remember { mutableStateOf(existingTask?.priority ?: Priority.MEDIUM) }
    var selectedDueOption by remember { mutableStateOf("Today") }
    var customDateMillis by remember { mutableStateOf(existingTask?.dueDate ?: System.currentTimeMillis()) }
    var tags by remember { mutableStateOf(existingTask?.tags?.joinToString(", ") ?: "") }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    var estimatedHours by remember { mutableStateOf(existingTask?.estimatedHours?.toString() ?: "") }
    var showError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId != null) "Edit Task" else "Add New Task") },
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
            // Task Name
            OutlinedTextField(
                value = taskName,
                onValueChange = {
                    taskName = it
                    showError = false
                },
                label = { Text("Task Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = showError && taskName.isBlank(),
                supportingText = {
                    if (showError && taskName.isBlank()) {
                        Text("Task name is required")
                    }
                }
            )

            // Task Type
            Text(
                text = "Task Type",
                style = MaterialTheme.typography.labelLarge
            )
            TaskTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )

            // Priority
            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelLarge
            )
            PrioritySelector(
                selectedPriority = selectedPriority,
                onPrioritySelected = { selectedPriority = it }
            )

            // Due Date
            Text(
                text = "Due Date",
                style = MaterialTheme.typography.labelLarge
            )
            DueDateSelector(
                selectedOption = selectedDueOption,
                onOptionSelected = { selectedDueOption = it },
                customDate = customDateMillis,
                onCustomDateChanged = { customDateMillis = it },
                onCustomDateClick = { showDatePicker = true }  // Always show picker when Custom clicked
            )

            // Tags
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma-separated)") },
                placeholder = { Text("frontend, backend, testing") },
                modifier = Modifier.fillMaxWidth()
            )

            // Estimated Hours
            OutlinedTextField(
                value = estimatedHours,
                onValueChange = { estimatedHours = it.filter { char -> char.isDigit() } },
                label = { Text("Estimated Hours") },
                placeholder = { Text("8") },
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            // Save Button
            Button(
                onClick = {
                    if (taskName.isBlank()) {
                        showError = true
                    } else {
                        val calendar = Calendar.getInstance()
                        val dueDateMillis = when (selectedDueOption) {
                            "Today" -> {
                                calendar.apply {
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                }.timeInMillis
                            }
                            "Tomorrow" -> {
                                calendar.apply {
                                    add(Calendar.DAY_OF_YEAR, 1)
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                }.timeInMillis
                            }
                            "This Week" -> {
                                calendar.apply {
                                    add(Calendar.DAY_OF_YEAR, 7)
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                }.timeInMillis
                            }
                            else -> customDateMillis
                        }

                        val tagsList = tags.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        val task = Task(
                            id = taskId ?: "",
                            name = taskName,
                            description = description,
                            type = selectedType,
                            priority = selectedPriority,
                            dueDate = dueDateMillis,
                            tags = tagsList,
                            estimatedHours = estimatedHours.toIntOrNull() ?: 0,
                            isCompleted = existingTask?.isCompleted ?: false,
                            completedAt = existingTask?.completedAt
                        )

                        if (taskId != null) {
                            viewModel.updateTask(task)
                        } else {
                            viewModel.addTask(task)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (taskId != null) "Update Task" else "Add Task")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        customDateMillis = millis
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun TaskTypeSelector(
    selectedType: TaskType,
    onTypeSelected: (TaskType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskType.entries.take(3).forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.name) },
                modifier = Modifier.weight(1f)
            )
        }
    }
    if (TaskType.entries.size > 3) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskType.entries.drop(3).forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.name) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Priority.entries.forEach { priority ->
            FilterChip(
                selected = selectedPriority == priority,
                onClick = { onPrioritySelected(priority) },
                label = { Text(priority.name) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DueDateSelector(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    customDate: Long,
    onCustomDateChanged: (Long) -> Unit,
    onCustomDateClick: () -> Unit = {}  // NEW: Callback to trigger date picker
) {
    val options = listOf("Today", "Tomorrow", "This Week", "Custom")
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedOption == option,
                    onClick = {
                        onOptionSelected(option)
                        // FIX: Always trigger date picker when Custom is clicked
                        if (option == "Custom") {
                            onCustomDateClick()
                        }
                    },
                    label = { Text(option) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (selectedOption == "Custom") {
            // Make the date text clickable to re-open the picker
            TextButton(
                onClick = { onCustomDateClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Selected: ${dateFormatter.format(customDate)} (tap to change)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Preview is not available for this screen as it requires a ViewModel instance
// Use the actual app to test this screen