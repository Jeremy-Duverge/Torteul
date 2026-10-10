package com.totoprout.torteul.data.network

import android.util.Xml
import com.totoprout.torteul.data.local.entities.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.InputStream
import java.time.OffsetDateTime

object RssService {
    private const val FEED_URL = "https://www.youtube.com/feeds/videos.xml?channel_id="
    private const val ATOM = "http://www.w3.org/2005/Atom"
    private const val YT = "http://www.youtube.com/xml/schemas/2015"
    private const val MEDIA = "http://search.yahoo.com/mrss/"

    suspend fun fetchVideos(client: OkHttpClient, channelId: String): List<VideoEntity> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(FEED_URL + channelId).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code} for channel $channelId")
                }
                val body = response.body ?: throw IOException("Empty feed for channel $channelId")
                parse(Xml.newPullParser(), body.byteStream(), channelId)
            }
        }

    /** Shorts are dropped: the feed links them as /shorts/ instead of /watch. */
    fun parse(parser: XmlPullParser, input: InputStream, channelId: String): List<VideoEntity> {
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(input, null)
        val videos = mutableListOf<VideoEntity>()
        var entry: Entry? = null
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            val current = entry
            when {
                parser.eventType == XmlPullParser.START_TAG && parser.isAtom("entry") -> entry = Entry()
                parser.eventType == XmlPullParser.START_TAG && current != null -> current.read(parser)
                parser.eventType == XmlPullParser.END_TAG && parser.isAtom("entry") -> {
                    current?.toVideo(channelId)?.let(videos::add)
                    entry = null
                }
            }
        }
        return videos
    }

    private fun XmlPullParser.isAtom(tag: String) = namespace == ATOM && name == tag

    private class Entry {
        private var videoId = ""
        private var title = ""
        private var published = ""
        private var link = ""
        private var thumbnail = ""

        fun read(parser: XmlPullParser) {
            when (parser.namespace to parser.name) {
                YT to "videoId" -> videoId = parser.nextText()
                ATOM to "title" -> title = parser.nextText()
                ATOM to "published" -> published = parser.nextText()
                ATOM to "link" -> link = parser.getAttributeValue(null, "href").orEmpty()
                MEDIA to "thumbnail" -> thumbnail = parser.getAttributeValue(null, "url").orEmpty()
            }
        }

        fun toVideo(channelId: String): VideoEntity? {
            if (videoId.isEmpty() || "/shorts/" in link) return null
            val publishedAt = runCatching { OffsetDateTime.parse(published).toInstant().toEpochMilli() }
                .getOrNull() ?: return null
            return VideoEntity(
                id = videoId,
                title = title,
                publishedAt = publishedAt,
                channelId = channelId,
                thumbnailUrl = thumbnail,
            )
        }
    }
}
