package com.example.dopamindetox.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
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
    data object AddGoal : Screen("add_goal", "목표 추가")
    data object Recommend : Screen("recommend", "목표 추천")
}

@Composable
fun AppNavHost(mainVm: MainViewModel, padding: PaddingValues) {
    val navController = rememberNavController()
    val items = listOf(Screen.Main, Screen.Analysis, Screen.Todo)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in items.map { it.route }
    val showTopBar = currentRoute == Screen.Main.route || currentRoute == Screen.Analysis.route

    val startDestination = Screen.First.route

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(title = { Text("DopaMindetox") })
            }
        },
        bottomBar = {
            if (showBottomBar) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            composable(Screen.First.route) {
                FirstEntryScreen(onContinue = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.First.route) { inclusive = true }
                    }
                }, vm = mainVm)
            }

            // 📌 모든 하위 화면에 일관되게 padding 전달
            composable(Screen.Main.route) {
                MainScreen(vm = mainVm, navController = navController, padding = innerPadding)
            }
            composable(Screen.Analysis.route) {
                AnalysisScreen(vm = mainVm, navController = navController, padding = innerPadding)
            }
            composable(Screen.Todo.route) { 
                TodoScreen(vm = mainVm, navController = navController, padding = innerPadding)
            }
            composable(
                route = Screen.AddGoal.route + "/{dateKey}"
            ) { backStackEntry ->

                val dateKey = backStackEntry.arguments?.getString("dateKey") ?: "99991231"

                AddGoalScreen(
                    vm = mainVm,
                    navController = navController,
                    padding = innerPadding,
                    dateKey = dateKey
                )
            }

            composable(Screen.Recommend.route) {
                RecommendScreen(vm = mainVm, navController = navController, padding = innerPadding)
            }
        }
    }
}
