package com.totoprout.torteul.data.network

import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfo
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
}
