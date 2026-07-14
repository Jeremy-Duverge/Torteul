package com.totoprout.torteul.data.local.dao

import androidx.room.*
import com.totoprout.torteul.data.local.entities.CategoryEntity
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.data.local.entities.VideoEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY publishedAt DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE channelId = :channelId ORDER BY publishedAt DESC")
    fun getVideosByChannel(channelId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE category = :category ORDER BY publishedAt DESC")
    fun getRankedVideosByCategory(category: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE category IN (:categories) ORDER BY publishedAt DESC")
    fun getVideosByCategories(categories: List<String>): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideosById(id: String)
}
