package com.totoprout.torteul.data.local.dao

import androidx.room.*
import com.totoprout.torteul.data.local.entities.CategoryEntity
import com.totoprout.torteul.data.local.entities.ChannelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE id = :id")
    fun getChannelById(id: String): Flow<ChannelEntity>

    @Query("SELECT * FROM channels")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE category = :category")
    fun getChannelsByCategory(category: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)
}
