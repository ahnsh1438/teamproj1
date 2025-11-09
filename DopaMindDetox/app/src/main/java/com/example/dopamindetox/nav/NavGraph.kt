package com.example.dopamindetox.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.dopamindetox.ui.screens.*
import com.example.dopamindetox.vm.MainViewModel

sealed class Screen(val route: String, val label: String) {
    data object Main : Screen("main", "메인")
    data object Analysis : Screen("analysis", "분석")
    data object Todo : Screen("todo", "투두")
    data object First : Screen("first", "권한")
}

@Composable
fun AppNavHost(mainVm: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Main, Screen.Analysis, Screen.Todo)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val canGoBack by remember { derivedStateOf { navController.previousBackStackEntry != null } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DopaMindDetox") },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { scr ->
                    NavigationBarItem(
                        selected = currentRoute == scr.route,
                        onClick = {
                            navController.navigate(scr.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(scr.label) },
                        icon = {
                            when (scr) {
                                Screen.Main -> Icon(Icons.Filled.Home, null)
                                Screen.Analysis -> Icon(Icons.Filled.Insights, null)
                                Screen.Todo -> Icon(Icons.Filled.Checklist, null)
                                else -> Icon(Icons.Filled.Home, null)
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.First.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.First.route) {
                FirstEntryScreen(onContinue = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.First.route) { inclusive = true }
                    }
                }, vm = mainVm)
            }
            composable(Screen.Main.route) { MainScreen(vm = mainVm) }
            composable(Screen.Analysis.route) { AnalysisScreen(vm = mainVm) }
            composable(Screen.Todo.route) { TodoScreen(vm = mainVm) }
        }
    }
}
