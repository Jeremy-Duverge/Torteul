@file:OptIn(ExperimentalMaterial3Api::class)

package com.totoprout.torteul.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.totoprout.torteul.TorteulApplication
import com.totoprout.torteul.data.local.FeedItem
import com.totoprout.torteul.data.local.entities.CategoryEntity
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.ui.viewmodel.YouTubeViewModel
import kotlinx.coroutines.launch

private const val NEWPIPE_PACKAGE = "org.schabi.newpipe"

private enum class Tab(val label: String, val icon: ImageVector) {
    Feed("Feed", Icons.Default.Home),
    Channels("Channels", Icons.Default.Person),
    Categories("Categories", Icons.AutoMirrored.Filled.List),
}

@Composable
fun YouTubeMainScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as TorteulApplication
    val viewModel: YouTubeViewModel = viewModel(factory = YouTubeViewModel.Factory(app.youtubeRepository))
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableStateOf(Tab.Feed) }
    var menuOpen by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::exportTo) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importFrom) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }
    BackHandler(enabled = tab != Tab.Feed) { tab = Tab.Feed }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YouTube") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !refreshing) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Export subscriptions") },
                                onClick = {
                                    menuOpen = false
                                    exportLauncher.launch("torteul-subscriptions.json")
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Import subscriptions") },
                                onClick = {
                                    menuOpen = false
                                    importLauncher.launch(arrayOf("*/*"))
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (refreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
            when (tab) {
                Tab.Feed -> FeedTab(viewModel) { videoId ->
                    if (!openInNewPipe(context, videoId)) {
                        scope.launch { snackbarHostState.showSnackbar("Install NewPipe (F-Droid) to play videos") }
                    }
                }
                Tab.Channels -> ChannelsTab(viewModel)
                Tab.Categories -> CategoriesTab(viewModel)
            }
        }
    }
}

@Composable
private fun FeedTab(viewModel: YouTubeViewModel, onPlay: (String) -> Unit) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selected by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val feed by viewModel.feed.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        if (categories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories, key = { it.name }) { category ->
                    FilterChip(
                        selected = category.name in selected,
                        onClick = { viewModel.toggleCategory(category.name) },
                        label = { Text(category.name) },
                    )
                }
            }
        }
        if (feed.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No videos yet. Add channels in the Channels tab.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(feed, key = { it.video.id }) { item ->
                    VideoCard(item, onClick = { onPlay(item.video.id) })
                }
            }
        }
    }
}

@Composable
private fun VideoCard(item: FeedItem, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.video.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )
        Column(Modifier.padding(12.dp)) {
            Text(
                item.video.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOf(item.channelTitle, item.category, relativeTime(item.video.publishedAt))
                    .filter { it.isNotEmpty() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChannelsTab(viewModel: YouTubeViewModel) {
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        AddRow(label = "@handle, channel URL or ID", onAdd = viewModel::addChannel)
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(channels, key = { it.id }) { channel ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { editingId = channel.id }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(channel.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            channel.category.ifEmpty { "No category" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    channels.find { it.id == editingId }?.let { channel ->
        ChannelDialog(
            channel = channel,
            categories = categories,
            onCategory = { viewModel.setCategory(channel, it) },
            onUnsubscribe = {
                viewModel.unsubscribe(channel)
                editingId = null
            },
            onDismiss = { editingId = null },
        )
    }
}

@Composable
private fun ChannelDialog(
    channel: ChannelEntity,
    categories: List<CategoryEntity>,
    onCategory: (String) -> Unit,
    onUnsubscribe: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(channel.title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                (listOf("") + categories.map { it.name }).forEach { name ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = channel.category == name, onClick = { onCategory(name) })
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = channel.category == name, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(name.ifEmpty { "No category" })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onUnsubscribe) {
                Text("Unsubscribe", color = MaterialTheme.colorScheme.error)
            }
        },
    )
}

@Composable
private fun CategoriesTab(viewModel: YouTubeViewModel) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        AddRow(label = "New category", onAdd = viewModel::addCategory)
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories, key = { it.name }) { category ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(start = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(category.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { viewModel.deleteCategory(category) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete ${category.name}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRow(label: String, onAdd: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                onAdd(text.trim())
                text = ""
            },
            enabled = text.isNotBlank(),
        ) { Text("Add") }
    }
}

private fun relativeTime(epochMillis: Long): String =
    DateUtils.getRelativeTimeSpanString(epochMillis, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        .toString()

private fun openInNewPipe(context: Context, videoId: String): Boolean = try {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$videoId".toUri())
            .setPackage(NEWPIPE_PACKAGE)
    )
    true
} catch (e: ActivityNotFoundException) {
    false
}
