package com.totoprout.torteul.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels",
    indices = [Index(value = ["category"])]
)
data class ChannelEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String = "",
)
