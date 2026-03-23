package com.weighttracker.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Entry : Screen("entry")
    data object Trends : Screen("trends")
    data object Data : Screen("data")
    data object Settings : Screen("settings")
    data object History : Screen("history")
}
