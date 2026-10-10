package com.totoprout.torteul.data.local

import androidx.room.Embedded
import com.totoprout.torteul.data.local.entities.VideoEntity

data class FeedItem(
    @Embedded val video: VideoEntity,
    val channelTitle: String,
    val category: String,
)
