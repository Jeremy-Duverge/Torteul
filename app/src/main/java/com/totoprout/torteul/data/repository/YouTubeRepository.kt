package com.totoprout.torteul.data.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
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

    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    fun getAllChannels(): Flow<List<ChannelEntity>> {
        return channelDao.getAllChannels()
    }

    fun getChannelsByCategory(category: String): Flow<List<ChannelEntity>> {
        return channelDao.getChannelsByCategory(category)
    }

    fun getAllVideos(): Flow<List<VideoEntity>> {
        return videoDao.getAllVideos()
    }

    fun getVideosByChannel(channelId: String): Flow<List<VideoEntity>> {
        return videoDao.getVideosByChannel(channelId)
    }

    fun getVideosByCategory(category: String): Flow<List<VideoEntity>> {
        return videoDao.getRankedVideosByCategory(category)
    }

    fun getVideosByCategories(categories: List<String>): Flow<List<VideoEntity>> {
        return videoDao.getVideosByCategories(categories)
    }

    suspend fun assignCategoryToChannel(channel: ChannelEntity, category: String) {
        channel.category = category
        channelDao.updateChannel(channel)
        for (video in videoDao.getVideosByChannel(channel.id).first()) {
            video.category = category
            videoDao.updateVideo(video)
        }
    }

    suspend fun addCategory(name: String) {
        categoryDao.insertCategory(CategoryEntity(name))
    }

    suspend fun addChannel(handle: String, context: Context) {
        try {
            Log.d("YouTubeRepository", "Adding channel: $handle")

            val channelUrl = if (handle.startsWith("https://")) handle else "https://www.youtube.com/$handle"
            val channelInfo = NewPipeService.getChannelInfo(channelUrl)
            
            if (channelInfo != null) {
                Log.d("YouTubeRepository", "Found channel: ${channelInfo.name}")
                val channel = ChannelEntity(
                    id = channelInfo.id,
                    title = channelInfo.name
                )
                channelDao.insertChannel(channel)
                Toast.makeText(context, "Channel added: ${channelInfo.name}", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("YouTubeRepository", "Channel not found or error occurred for $handle")
                Toast.makeText(context, "Channel not found or error occurred", Toast.LENGTH_SHORT).show()
            }
        } catch (t: Throwable) {
            Log.e("YouTubeRepository", "Fatal error adding channel $handle", t)
            Toast.makeText(context, "Fatal error adding channel $handle", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun refreshVideos() {
        try {
            val channels = channelDao.getAllChannels().first()
            for (channel in channels) {
                try {
                    val rssVideos = RssService.getLatestVideos(channel.id, client)
                    val updatedVideos = rssVideos.map { it.copy(channelId = channel.id, category = channel.category) }
                    videoDao.insertVideos(updatedVideos)
                } catch (t: Throwable) {
                    Log.e("YouTubeRepository", "Error refreshing videos for channel ${channel.title}", t)
                }
            }
        } catch (t: Throwable) {
            Log.e("YouTubeRepository", "Fatal error in refreshVideos", t)
        }
    }
}
