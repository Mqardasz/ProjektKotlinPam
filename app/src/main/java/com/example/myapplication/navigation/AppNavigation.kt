package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.SensorRepository
import com.example.myapplication.ui.DashboardScreen
import com.example.myapplication.ui.HistoryScreen

@Composable
fun AppNavigation(repository: SensorRepository) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = DashboardRoute
    ) {
        composable<DashboardRoute> {
            DashboardScreen(
                repository = repository,
                onNavigateToHistory = {
                    navController.navigate(HistoryRoute)
                }
            )
        }
        
        composable<HistoryRoute> {
            HistoryScreen(
                repository = repository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
