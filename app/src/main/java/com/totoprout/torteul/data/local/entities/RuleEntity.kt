package com.totoprout.torteul.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["name"],
            childColumns = ["category"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["category"])]
)
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // e.g., "SUPPRESS_CHANNEL", "FILTER_KEYWORD"
    val pattern: String,
    val category: String? = null
)
