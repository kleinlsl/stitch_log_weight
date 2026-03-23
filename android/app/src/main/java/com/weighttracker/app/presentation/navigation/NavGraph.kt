package com.weighttracker.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.weighttracker.app.presentation.screens.home.HomeScreen
import com.weighttracker.app.presentation.screens.entry.EntryScreen
import com.weighttracker.app.presentation.screens.trends.TrendsScreen
import com.weighttracker.app.presentation.screens.data.DataScreen
import com.weighttracker.app.presentation.screens.settings.SettingsScreen
import com.weighttracker.app.presentation.screens.history.HistoryScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    onNavigateToEntry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val onNavigateToSettings = { navController.navigate(Screen.Settings.route) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onFabClick = onNavigateToEntry,
                onSettingsClick = onNavigateToSettings
            )
        }
        composable(Screen.Entry.route) {
            EntryScreen(onClose = onNavigateBack)
        }
        composable(Screen.Trends.route) {
            TrendsScreen(
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        composable(Screen.Data.route) {
            DataScreen(onSettingsClick = onNavigateToSettings)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = onNavigateBack)
        }
        composable(Screen.History.route) {
            HistoryScreen(onBack = onNavigateBack)
        }
    }
}
