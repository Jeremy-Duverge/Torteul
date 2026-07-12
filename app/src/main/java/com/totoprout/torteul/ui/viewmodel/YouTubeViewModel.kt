package com.totoprout.torteul.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.data.local.entities.VideoEntity
import com.totoprout.torteul.data.repository.YouTubeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class YouTubeViewModel(private val repository: YouTubeRepository) : ViewModel() {

    fun getVideos(categoryId: Long): StateFlow<List<VideoEntity>> {
        return repository.getVideos(categoryId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun refreshVideos(categoryId: Long) {
        viewModelScope.launch {
            repository.refreshVideos(categoryId)
        }
    }

    fun addChannel(handle: String, categoryId: Long, context: Context) {
        viewModelScope.launch {
            repository.addChannel(handle, categoryId, context)
        }
    }

    fun getChannels(categoryId: Long): StateFlow<List<ChannelEntity>> {
        return repository.getChannels(categoryId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())}

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
