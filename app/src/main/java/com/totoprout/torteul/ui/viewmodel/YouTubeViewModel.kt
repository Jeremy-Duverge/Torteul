package com.totoprout.torteul.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.totoprout.torteul.data.local.entities.CategoryEntity
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.data.local.entities.VideoEntity
import com.totoprout.torteul.data.repository.YouTubeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class YouTubeViewModel(private val repository: YouTubeRepository) : ViewModel() {

    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())
    val selectedCategories = _selectedCategories.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<ChannelEntity>> = repository.getAllChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideos: StateFlow<List<VideoEntity>> = repository.getAllVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredVideos: StateFlow<List<VideoEntity>> = _selectedCategories
        .flatMapLatest { categories ->
            if (categories.isEmpty()) {
                repository.getAllVideos()
            } else {
                repository.getVideosByCategories(categories)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleCategory(category: String) {
        val current = _selectedCategories.value.toMutableList()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _selectedCategories.value = current
    }

    fun addChannel(handle: String, context: Context) {
        viewModelScope.launch {
            repository.addChannel(handle, context)
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.addCategory(name)
        }
    }

    fun assignCategoryToChannel(channel: ChannelEntity, category: String) {
        viewModelScope.launch {
            repository.assignCategoryToChannel(channel, category)
        }
    }

    fun refreshVideos() {
        viewModelScope.launch {
            repository.refreshVideos()
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
