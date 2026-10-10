package com.totoprout.torteul.data.local.dao

import androidx.room.*
import com.totoprout.torteul.data.local.FeedItem
import com.totoprout.torteul.data.local.entities.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query(
        """
        SELECT v.*, c.title AS channelTitle, c.category AS category
        FROM videos v JOIN channels c ON c.id = v.channelId
        ORDER BY v.publishedAt DESC
        """
    )
    fun getFeed(): Flow<List<FeedItem>>

    @Query(
        """
        SELECT v.*, c.title AS channelTitle, c.category AS category
        FROM videos v JOIN channels c ON c.id = v.channelId
        WHERE c.category IN (:categories)
        ORDER BY v.publishedAt DESC
        """
    )
    fun getFeedForCategories(categories: List<String>): Flow<List<FeedItem>>

    @Upsert
    suspend fun upsertVideos(videos: List<VideoEntity>)

    @Query("DELETE FROM videos WHERE channelId = :channelId")
    suspend fun deleteByChannel(channelId: String)
}
