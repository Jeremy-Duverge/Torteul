package com.totoprout.torteul.data.network

import android.util.Log
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NewPipeService {
    private var initialized = false

    fun init(client: OkHttpClient) {
        if (!initialized) {
            NewPipe.init(OkHttpDownloader(client))
            initialized = true
        }
    }

    suspend fun getChannelInfo(channelUrl: String): ChannelInfo = withContext(Dispatchers.IO) {
        val service = ServiceList.YouTube
        val extractor = service.getChannelExtractor(channelUrl)
        extractor.fetchPage()
        ChannelInfo.getInfo(extractor)
    }

    suspend fun getLatestVideos(channelUrl: String): List<StreamInfoItem> = withContext(Dispatchers.IO) {
        try {
            Log.d("NewPipeService", "Fetching latest videos for: $channelUrl")
            val service = ServiceList.YouTube
            val extractor = service.getChannelExtractor(channelUrl)
            extractor.fetchPage()
            
            val videoTab = extractor.tabs.find { it.id == "videos" } ?: extractor.tabs.firstOrNull()
            if (videoTab != null) {
                val tabExtractor = service.getChannelTabExtractor(videoTab) as ChannelTabExtractor
                tabExtractor.fetchPage()
                val items = tabExtractor.getInitialPage().items
                    .filterIsInstance<StreamInfoItem>()
                Log.d("NewPipeService", "Found ${items.size} videos")
                items
            } else {
                Log.w("NewPipeService", "No video tab found for: $channelUrl")
                emptyList()
            }
        } catch (t: Throwable) {
            Log.e("NewPipeService", "Error in getLatestVideos for $channelUrl", t)
            emptyList()
        }
    }
}
