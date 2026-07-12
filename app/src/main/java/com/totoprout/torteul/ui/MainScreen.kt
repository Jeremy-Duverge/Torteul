package com.totoprout.torteul.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.totoprout.torteul.TorteulApplication
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.data.local.entities.VideoEntity
import com.totoprout.torteul.ui.viewmodel.YouTubeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as TorteulApplication
    val youtubeViewModel: YouTubeViewModel = viewModel(
        factory = YouTubeViewModel.Factory(app.youtubeRepository)
    )

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToYouTube = { navController.navigate("youtube") },
                onNavigateToAlarms = { navController.navigate("alarms") },
                onNavigateToCalendar = { navController.navigate("calendar") }
            )
        }
        composable("youtube") {
            YouTubeScreen(
                viewModel = youtubeViewModel,
                onBack = { navController.popBackStack() },
                onPlayVideo = { videoId ->
                    val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                        putExtra("VIDEO_ID", videoId)
                    }
                    context.startActivity(intent)
                },
                context = context
            )
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
fun YouTubeScreen(
    viewModel: YouTubeViewModel,
    onBack: () -> Unit,
    onPlayVideo: (String) -> Unit,
    context: Context
) {
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
                1 -> YouTubeChannelsTab(viewModel, context)
                2 -> YouTubeLatestTab(viewModel, onPlayVideo)
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
fun YouTubeChannelsTab(viewModel: YouTubeViewModel, context: Context) {
    var handle by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val channels by viewModel.getChannels(1L).collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Channels Tab", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = handle,
                onValueChange = { handle = it },
                label = { Text("Channel Handle (e.g. @TheCherno)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (handle.isNotEmpty()) {
                        viewModel.addChannel(handle, 1L, context)
                        handle = ""
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Add Channel")
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(channels, key = { it.youtubeId }) { channel ->
                    ChannelItem(channel, onClick = {})
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun YouTubeLatestTab(viewModel: YouTubeViewModel, onPlayVideo: (String) -> Unit) {
    val videos by viewModel.getVideos(1L).collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshVideos(1L)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Latest Tab", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        
        if (videos.isEmpty()) {
            Text("No videos found. Try adding a channel first.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(videos, key = { it.youtubeId }) { video ->
                    VideoItem(video, onClick = { onPlayVideo(video.youtubeId) })
                }
            }
        }
    }
}

@Composable
fun VideoItem(video: VideoEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = video.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "ID: ${video.youtubeId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChannelItem(channel: ChannelEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = channel.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "ID: ${channel.youtubeId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
