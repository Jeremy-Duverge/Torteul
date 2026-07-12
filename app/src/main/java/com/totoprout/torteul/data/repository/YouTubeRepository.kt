package com.totoprout.torteul.data.repository

import android.util.Log
import com.totoprout.torteul.data.local.TorteulDatabase
import com.totoprout.torteul.data.local.entities.CategoryEntity
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.data.local.entities.VideoEntity
import com.totoprout.torteul.data.network.NewPipeService
import com.totoprout.torteul.data.network.RssService
import okhttp3.OkHttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class YouTubeRepository(
    private val db: TorteulDatabase,
    private val client: OkHttpClient
) {
    private val categoryDao = db.categoryDao()
    private val channelDao = db.channelDao()
    private val videoDao = db.videoDao()

    suspend fun addChannel(handle: String, categoryId: Long) {
        try {
            Log.d("YouTubeRepository", "Adding channel: $handle")
            
            // Ensure category exists to avoid FK constraint failure
            val categories = categoryDao.getAllCategories().first()
            if (categories.none { it.id == categoryId }) {
                Log.d("YouTubeRepository", "Category $categoryId not found, creating default")
                categoryDao.insertCategory(CategoryEntity(id = categoryId, name = "Default", displayOrder = 0))
            }

            val channelUrl = if (handle.startsWith("https://")) handle else "https://www.youtube.com/$handle"
            val channelInfo = NewPipeService.getChannelInfo(channelUrl)
            
            if (channelInfo != null) {
                Log.d("YouTubeRepository", "Found channel: ${channelInfo.name}")
                val channel = ChannelEntity(
                    youtubeId = channelInfo.id,
                    title = channelInfo.name,
                    categoryId = categoryId
                )
                channelDao.insertChannel(channel)
            } else {
                Log.e("YouTubeRepository", "Channel not found or error occurred for $handle")
            }
        } catch (t: Throwable) {
            Log.e("YouTubeRepository", "Fatal error adding channel $handle", t)
        }
    }

    suspend fun refreshVideos(categoryId: Long) {
        try {
            val channels = channelDao.getChannelsByCategory(categoryId).first()
            for (channel in channels) {
                try {
                    val rssVideos = RssService.getLatestVideos(channel.youtubeId, client)
                    val updatedVideos = rssVideos.map { it.copy(channelId = channel.id) }
                    videoDao.insertVideos(updatedVideos)
                } catch (t: Throwable) {
                    Log.e("YouTubeRepository", "Error refreshing videos for channel ${channel.title}", t)
                }
            }
        } catch (t: Throwable) {
            Log.e("YouTubeRepository", "Fatal error in refreshVideos", t)
        }
    }

    fun getVideos(categoryId: Long): Flow<List<VideoEntity>> {
        return videoDao.getRankedVideosByCategory(categoryId)
    }
}
