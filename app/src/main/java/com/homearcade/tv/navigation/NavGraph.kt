package com.homearcade.tv.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homearcade.tv.data.ServerRepository
import com.homearcade.tv.ui.screens.HomeScreen
import com.homearcade.tv.ui.screens.SettingsScreen
import com.homearcade.tv.ui.screens.SetupScreen
import com.homearcade.tv.ui.screens.SplashScreen
import com.homearcade.tv.viewmodel.SetupViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val setupViewModel: SetupViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onFinished = {
                    val config = setupViewModel.getSavedConfig()
                    if (config != null && config.host.isNotBlank()) {
                        navController.navigate("home/${config.host}/${config.port}") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("setup") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("setup") {
            val uiState by setupViewModel.uiState.collectAsState()
            SetupScreen(
                host = uiState.host,
                port = uiState.port,
                connectionStatus = uiState.connectionStatus,
                onHostChange = { setupViewModel.updateHost(it) },
                onPortChange = { setupViewModel.updatePort(it) },
                onTestConnection = { setupViewModel.testConnection() },
                onLaunch = { setupViewModel.saveAndLaunch { h, p ->
                    navController.navigate("home/$h/$p") {
                        popUpTo("setup") { inclusive = false }
                    }
                }}
            )
        }

        composable(
            route = "home/{host}/{port}",
            arguments = listOf(
                navArgument("host") { type = NavType.StringType },
                navArgument("port") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val host = backStackEntry.arguments?.getString("host") ?: ""
            val port = backStackEntry.arguments?.getString("port") ?: "9876"
            HomeScreen(
                host = host,
                port = port,
                onDisconnect = {
                    navController.navigate("setup") {
                        popUpTo("setup") { inclusive = true }
                    }
                },
                onSettings = {
                    navController.navigate("settings/$host/$port")
                }
            )
        }

        composable(
            route = "settings/{host}/{port}",
            arguments = listOf(
                navArgument("host") { type = NavType.StringType },
                navArgument("port") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val host = backStackEntry.arguments?.getString("host") ?: ""
            val port = backStackEntry.arguments?.getString("port") ?: "9876"
            SettingsScreen(
                host = host,
                port = port,
                onBack = { navController.popBackStack() },
                onSave = { newHost, newPort ->
                    navController.navigate("home/$newHost/$newPort") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            )
        }
    }
}
