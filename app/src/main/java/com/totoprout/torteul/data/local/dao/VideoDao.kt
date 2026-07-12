package com.totoprout.torteul.data.local.dao

import androidx.room.*
import com.totoprout.torteul.data.local.entities.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("""
        SELECT v.* FROM videos v
        INNER JOIN channels c ON v.channelId = c.id
        WHERE c.categoryId = :categoryId
        ORDER BY v.rankScore DESC, v.publishedAt DESC
    """)
    fun getRankedVideosByCategory(categoryId: Long): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("DELETE FROM videos WHERE channelId IN (SELECT id FROM channels WHERE categoryId = :categoryId)")
    suspend fun deleteVideosByCategory(categoryId: Long)
}
