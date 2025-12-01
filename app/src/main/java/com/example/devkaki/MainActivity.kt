// MainActivity.kt
package com.example.devkaki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.devkaki.ui.theme.DevKakiTheme
import com.example.devkaki.screens.SplashScreen
import com.example.devkaki.screens.HomeScreen
import com.example.devkaki.screens.AddTaskScreen
import com.example.devkaki.screens.AllTasksScreen
import com.example.devkaki.screens.TaskDetailScreen
import com.example.devkaki.screens.ProgressVisualizationScreen
import com.example.devkaki.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemIsDark = isSystemInDarkTheme()
            val userSetIsDark = remember { mutableStateOf<Boolean?>(null) }
            val isDarkTheme = userSetIsDark.value ?: systemIsDark

            DevKakiTheme(darkTheme = isDarkTheme) {
                DevKakiApp(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { userSetIsDark.value = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun DevKakiApp(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: TaskViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAddTask = { navController.navigate("addTask") },
                onNavigateToAllTasks = { navController.navigate("allTasks") },
                onNavigateToProgress = { navController.navigate("progress") },
                onNavigateToDetail = { taskId ->
                    navController.navigate("detail/$taskId")
                },
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }

        composable("addTask") {
            AddTaskScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                taskId = null
            )
        }

        composable("addTask/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            AddTaskScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                taskId = taskId
            )
        }

        composable("allTasks") {
            AllTasksScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { taskId ->
                    navController.navigate("detail/$taskId")
                }
            )
        }

        composable("detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                viewModel = viewModel,
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("addTask/$id")
                }
            )
        }

        composable("progress") {
            ProgressVisualizationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}