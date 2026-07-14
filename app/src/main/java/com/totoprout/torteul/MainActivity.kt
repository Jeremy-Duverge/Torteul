package com.totoprout.torteul

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.totoprout.torteul.ui.navigation.Route
import com.totoprout.torteul.ui.screens.HomeScreen
import com.totoprout.torteul.ui.screens.PlaceholderScreen
import com.totoprout.torteul.ui.screens.YouTubeMainScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Route.Home
                    ) {
                        composable<Route.Home> {
                            HomeScreen(navController = navController)
                        }
                        composable<Route.YouTube> {
                            YouTubeMainScreen(navController = navController)
                        }
                        composable<Route.Alarms> {
                            PlaceholderScreen("Alarms", navController = navController)
                        }
                        composable<Route.Calendar> {
                            PlaceholderScreen("Calendar", navController = navController)
                        }
                    }
                }
            }
        }
    }
}
