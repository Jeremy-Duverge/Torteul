package com.totoprout.torteul.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos",
    indices = [Index(value = ["channelId"]), Index(value = ["category"])]
)
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val publishedAt: Long, // Unix timestamp
    val channelId: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: Long, // Duration in seconds
    var category: String = "",
    val rankScore: Float = 0f
)
