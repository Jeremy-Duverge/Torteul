package com.totoprout.torteul.ui.screens

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.totoprout.torteul.TorteulApplication
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.data.local.entities.VideoEntity
import com.totoprout.torteul.ui.VideoPlayerActivity
import com.totoprout.torteul.ui.viewmodel.YouTubeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeMainScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as TorteulApplication
    val youtubeViewModel: YouTubeViewModel = viewModel(
        factory = YouTubeViewModel.Factory(app.youtubeRepository)
    )
    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = listOf("Search", "Home", "Latest")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YouTube Sub-App") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
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
                                1 -> Icon(Icons.Default.Home, contentDescription = null)
                                2 -> Icon(Icons.Default.DateRange, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            when (selectedTab) {
                0 -> YouTubeSearchTab()
                1 -> YouTubeHomeTab(youtubeViewModel)
                2 -> YouTubeLatestTab(youtubeViewModel)
            }
        }
    }
}

@Composable
fun YouTubeSearchTab() {
    val url = "https://www.youtube.com/results?search_query=climbing"
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun YouTubeHomeTab(viewModel: YouTubeViewModel) {
    var homeTab by remember { mutableStateOf("grid") }

    when (homeTab) {
        "grid" -> YouTubeHomeGridTab(onNavigate = { homeTab = it })
        "categories" -> YouTubeCategoriesTab(viewModel, onBack = { homeTab = "grid" })
        "channels" -> YouTubeChannelsTab(viewModel, onBack = { homeTab = "grid" })
        "videos" -> YouTubeVideosTab(viewModel, onBack = { homeTab = "grid" })
    }
}

@Composable
fun YouTubeHomeGridTab(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { onNavigate("categories") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.padding(8.dp))
            Text("Categories", style = MaterialTheme.typography.headlineSmall)
        }
        Button(
            onClick = { onNavigate("channels") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.padding(8.dp))
            Text("Channels", style = MaterialTheme.typography.headlineSmall)
        }
        Button(
            onClick = { onNavigate("videos") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.padding(8.dp))
            Text("Videos", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun Form(label: String, buttonLabel: String, onClick: (String) -> Unit) {
    var handle by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = handle,
            onValueChange = { handle = it },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { if (handle.isNotEmpty()) { onClick(handle) ; handle = ""  }},
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(buttonLabel)
        }
    }
}

@Composable
fun YouTubeChannelsTab(viewModel: YouTubeViewModel, onBack: () -> Unit) {
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedChannelForDetails by remember { mutableStateOf<ChannelEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Back", modifier = Modifier.aspectRatio(1f))
            }
            Text("Channels", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.height(16.dp))

        Form("Channel Handle (e.g. @TheCherno)", "Add Channel"
        ) { handle -> viewModel.addChannel(handle, context) }

        Spacer(Modifier.height(16.dp))

        ChannelList(channels) { channel ->
            selectedChannelForDetails = channel
        }
    }

    selectedChannelForDetails?.let { channel ->
        Dialog(onDismissRequest = { selectedChannelForDetails = null }) {
            ChannelDetailsCard(channel)
        }
    }
}

@Composable
fun YouTubeCategoriesTab(viewModel: YouTubeViewModel, onBack: () -> Unit) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Back")
            }
            Text("Categories", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.height(16.dp))

        Form("Category Name (e.g. \"Climbing\")", "Add Category", { handle -> viewModel.addCategory(handle) })

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories, key = { it.name }) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = category.name,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun YouTubeVideosTab(viewModel: YouTubeViewModel, onBack: () -> Unit) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val videos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Back")
            }
            Text("Videos", style = MaterialTheme.typography.headlineMedium)
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it.name }) { category ->
                val isSelected = selectedCategories.contains(category.name)
                Button(
                    onClick = { viewModel.toggleCategory(category.name) },
                    colors = if (isSelected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(category.name)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        VideoList(videos) { videoId ->
            val intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtra("videoId", videoId)
            context.startActivity(intent)
        }
    }
}

@Composable
fun YouTubeLatestTab(viewModel: YouTubeViewModel) {
    val videos by viewModel.allVideos.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshVideos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Latest Videos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No videos found. Try adding a channel first.")
            }
        } else {
            VideoList(videos) { videoId ->
                val intent = Intent(context, VideoPlayerActivity::class.java)
                intent.putExtra("videoId", videoId)
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun VideoList(videos: List<VideoEntity>, onPlayVideo: (String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(videos, key = { it.id }) { video ->
            VideoItem(video, onClick = { onPlayVideo(video.id) })
        }
    }
}

@Composable
fun ChannelList(channels: List<ChannelEntity>, onClick: (ChannelEntity) -> Unit = { } ) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(channels, key = { it.id }) { channel ->
            ChannelItem(channel, onClick = { onClick(channel) })
        }
    }
}

@Composable
fun ChannelDetailsCard(channel: ChannelEntity) {
    val context = LocalContext.current
    val app = context.applicationContext as TorteulApplication
    val youtubeViewModel: YouTubeViewModel = viewModel(
        factory = YouTubeViewModel.Factory(app.youtubeRepository)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = channel.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "ID: ${channel.id}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Category: ${channel.category}", style = MaterialTheme.typography.bodySmall)
            Form("Existing category Name (e.g. \"Climbing\")", "Set Category", { handle -> youtubeViewModel.assignCategoryToChannel(channel, handle) })
        }
    }
}

@Composable
fun VideoItem(video: VideoEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = video.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Category: ${video.category.ifEmpty { "None" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChannelItem(channel: ChannelEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = channel.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "ID: ${channel.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
