package com.totoprout.torteul.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    fun addChannel(handle: String, categoryId: Long) {
        viewModelScope.launch {
            repository.addChannel(handle, categoryId)
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
