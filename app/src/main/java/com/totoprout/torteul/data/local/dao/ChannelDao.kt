package com.totoprout.torteul.data.local.dao

import androidx.room.*
import com.totoprout.torteul.data.local.entities.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE categoryId = :categoryId")
    fun getChannelsByCategory(categoryId: Long): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)
}
