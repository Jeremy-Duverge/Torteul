package com.totoprout.torteul.data.network

import android.util.Xml
import com.totoprout.torteul.data.local.entities.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

object RssService {
    private val ns: String? = null
    private val ytNs = "http://www.youtube.com/xml/schemas/2015"
    private val mediaNs = "http://search.yahoo.com/mrss/"

    suspend fun getLatestVideos(channelId: String, client: OkHttpClient): List<VideoEntity> = withContext(Dispatchers.IO) {
        val url = "https://www.youtube.com/feeds/videos.xml?channel_id=$channelId"
        val request = Request.Builder().url(url).build()
        
        client.newCall(request).execute().use { response ->
            val inputStream = response.body?.byteStream() ?: return@withContext emptyList()
            parse(inputStream, "") // channelPk is filled later by Repository
        }
    }

    private fun parse(inputStream: InputStream, channelPk: String): List<VideoEntity> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(inputStream, null)
        parser.nextTag()
        return readFeed(parser, channelPk)
    }

    private fun readFeed(parser: XmlPullParser, channelPk: String): List<VideoEntity> {
        val entries = mutableListOf<VideoEntity>()
        parser.require(XmlPullParser.START_TAG, ns, "feed")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "entry") {
                entries.add(readEntry(parser, channelPk))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    private fun readEntry(parser: XmlPullParser, channelPk: String): VideoEntity {
        parser.require(XmlPullParser.START_TAG, ns, "entry")
        var youtubeId = ""
        var title = ""
        var publishedAt = 0L

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "videoId" -> if (parser.namespace == ytNs) youtubeId = readText(parser, "videoId", ytNs) else skip(parser)
                "title" -> title = readText(parser, "title", ns)
                "published" -> {
                    val dateStr = readText(parser, "published", ns)
                    // Simplified: in real app use java.time or similar
                    publishedAt = System.currentTimeMillis() 
                }
                else -> skip(parser)
            }
        }
        return VideoEntity(
            id = youtubeId,
            title = title,
            publishedAt = publishedAt,
            channelId = channelPk,
            thumbnailUrl = "",
            videoUrl = "",
            duration = 0,
            category = ""
        )
    }

    private fun readText(parser: XmlPullParser, tagName: String, namespace: String?): String {
        parser.require(XmlPullParser.START_TAG, namespace, tagName)
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, namespace, tagName)
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) throw IllegalStateException()
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
