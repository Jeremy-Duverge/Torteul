package com.totoprout.torteul.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.totoprout.torteul.NativeLib

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToYouTube = { navController.navigate("youtube") },
                onNavigateToAlarms = { navController.navigate("alarms") },
                onNavigateToCalendar = { navController.navigate("calendar") }
            )
        }
        composable("youtube") {
            YouTubeScreen(onBack = { navController.popBackStack() })
        }
        composable("alarms") {
            PlaceholderScreen("Alarms", onBack = { navController.popBackStack() })
        }
        composable("calendar") {
            PlaceholderScreen("Calendar", onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToYouTube: () -> Unit,
    onNavigateToAlarms: () -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Torteul Home") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onNavigateToYouTube, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("YouTube")
            }
            Button(onClick = onNavigateToAlarms, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Alarms")
            }
            Button(onClick = onNavigateToCalendar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Calendar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Search", "Channels", "Latest")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YouTube Sub-App") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Search, contentDescription = null)
                                1 -> Icon(Icons.Default.Person, contentDescription = null)
                                2 -> Icon(Icons.Default.List, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            when (selectedTab) {
                0 -> YouTubeSearchTab()
                1 -> YouTubeChannelsTab()
                2 -> YouTubeLatestTab()
            }
        }
    }
}

@Composable
fun YouTubeSearchTab() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Search Tab", style = MaterialTheme.typography.headlineMedium)
        Text("Classical YouTube search page goes here.")
    }
}

@Composable
fun YouTubeChannelsTab() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Channels Tab", style = MaterialTheme.typography.headlineMedium)
        Text("List of followed channels.")
    }
}

@Composable
fun YouTubeLatestTab() {
    val latestVideos = remember { NativeLib.getYouTubeLatest() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Latest Tab", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Data fetched from C++ Engine:", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        
        latestVideos.forEach { video ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = video,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(name: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
