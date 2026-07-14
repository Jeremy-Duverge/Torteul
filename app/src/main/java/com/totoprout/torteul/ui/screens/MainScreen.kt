package com.totoprout.torteul.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

import com.totoprout.torteul.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Torteul Home") }) }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                HomeGrid(navController = navController)
            }
        }
    }
}

@Composable
fun HomeGrid(
    navController: NavController,
) {
    Column{
        Button(onClick = { navController.navigate(Route.YouTube) }) {
            Column {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Text("YouTube")
            }
        }
        Button(onClick = { navController.navigate(Route.Alarms) }) {
            Column {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Text("Alarms")
            }
        }
        Button(onClick = { navController.navigate(Route.Calendar) }) {
            Column {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Text("Calendar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(name: String, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigate(Route.Home) {
                            popUpTo(Route.Home) { inclusive = true }
                        } }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "home")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("$name Placeholder", style = MaterialTheme.typography.headlineLarge)
        }
    }
}