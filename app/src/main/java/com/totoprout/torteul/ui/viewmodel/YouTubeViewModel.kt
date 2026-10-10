package com.totoprout.torteul.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.totoprout.torteul.data.local.FeedItem
import com.totoprout.torteul.data.local.entities.CategoryEntity
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.data.repository.YouTubeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "YouTubeViewModel"

class YouTubeViewModel(private val repository: YouTubeRepository) : ViewModel() {

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<ChannelEntity>> = repository.channels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val feed: StateFlow<List<FeedItem>> = _selectedCategories
        .flatMapLatest { repository.feed(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.restoreIfEmpty()
            if (repository.isRefreshDue()) refresh()
        }
    }

    fun toggleCategory(name: String) {
        _selectedCategories.update { if (name in it) it - name else it + name }
    }

    fun refresh() {
        if (_refreshing.value) return
        _refreshing.value = true
        launchAction("Refresh failed") {
            try {
                val failed = repository.refreshVideos()
                if (failed.isEmpty()) null else "Couldn't refresh: ${failed.joinToString()}"
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun addChannel(input: String) = launchAction("Couldn't add $input") {
        "Added ${repository.addChannel(input)}"
    }

    fun unsubscribe(channel: ChannelEntity) = launchAction("Couldn't unsubscribe from ${channel.title}") {
        repository.unsubscribe(channel)
        "Unsubscribed from ${channel.title}"
    }

    fun setCategory(channel: ChannelEntity, category: String) = launchAction("Couldn't set the category") {
        repository.setCategory(channel, category)
        null
    }

    fun addCategory(name: String) = launchAction("Couldn't add $name") {
        repository.addCategory(name)
        null
    }

    fun deleteCategory(category: CategoryEntity) = launchAction("Couldn't delete ${category.name}") {
        repository.deleteCategory(category)
        _selectedCategories.update { it - category.name }
        null
    }

    fun exportTo(uri: Uri) = launchAction("Export failed") {
        "Exported ${repository.exportTo(uri)} channels"
    }

    fun importFrom(uri: Uri) = launchAction("Import failed") {
        val count = repository.importFrom(uri)
        refresh()
        "Imported $count channels"
    }

    /** Runs [action] and shows its result (or the failure) as a snackbar message. */
    private fun launchAction(failure: String, action: suspend () -> String?) {
        viewModelScope.launch {
            val message = try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, failure, e)
                "$failure: ${e.message ?: e.javaClass.simpleName}"
            }
            message?.let { _messages.send(it) }
        }
    }

    class Factory(private val repository: YouTubeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(YouTubeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return YouTubeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
