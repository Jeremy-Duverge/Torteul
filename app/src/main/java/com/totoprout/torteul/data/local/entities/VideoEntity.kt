package com.totoprout.torteul.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos",
    indices = [Index(value = ["channelId"])]
)
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val publishedAt: Long, // Unix timestamp
    val channelId: String,
    val thumbnailUrl: String,
)
