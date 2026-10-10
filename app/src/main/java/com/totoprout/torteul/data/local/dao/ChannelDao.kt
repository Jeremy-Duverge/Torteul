package com.totoprout.torteul.data.local.dao

import androidx.room.*
import com.totoprout.torteul.data.local.entities.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY title COLLATE NOCASE")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun count(): Int

    // IGNORE keeps the category when an existing channel is added again.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Upsert
    suspend fun upsertChannels(channels: List<ChannelEntity>)

    @Query("UPDATE channels SET category = :category WHERE id = :id")
    suspend fun setCategory(id: String, category: String)

    @Query("UPDATE channels SET category = '' WHERE category = :category")
    suspend fun clearCategory(category: String)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)
}
