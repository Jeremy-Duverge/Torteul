package com.totoprout.torteul.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos",
    foreignKeys = [
        ForeignKey(
            entity = ChannelEntity::class,
            parentColumns = ["id"],
            childColumns = ["channelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["channelId"])]
)
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val youtubeId: String,
    val title: String,
    val publishedAt: Long, // Unix timestamp
    val channelId: Long,
    val rankScore: Float = 0f
)
