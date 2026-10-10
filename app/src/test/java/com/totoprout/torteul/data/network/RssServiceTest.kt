package com.totoprout.torteul.data.network

import org.junit.Assert.assertEquals
import org.junit.Test
import org.kxml2.io.KXmlParser
import java.time.OffsetDateTime

class RssServiceTest {

    private val feed = """
        <?xml version="1.0" encoding="UTF-8"?>
        <feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns:media="http://search.yahoo.com/mrss/" xmlns="http://www.w3.org/2005/Atom">
         <title>Channel</title>
         <published>2010-01-01T00:00:00+00:00</published>
         <entry>
          <yt:videoId>vid1</yt:videoId>
          <title>A regular video</title>
          <link rel="alternate" href="https://www.youtube.com/watch?v=vid1"/>
          <author><name>Someone</name></author>
          <published>2026-10-08T21:00:40+00:00</published>
          <media:group>
           <media:title>Not the title</media:title>
           <media:thumbnail url="https://i4.ytimg.com/vi/vid1/hqdefault.jpg" width="480" height="360"/>
          </media:group>
         </entry>
         <entry>
          <yt:videoId>short1</yt:videoId>
          <title>A short</title>
          <link rel="alternate" href="https://www.youtube.com/shorts/short1"/>
          <published>2026-10-09T10:00:00+00:00</published>
         </entry>
        </feed>
    """.trimIndent()

    @Test
    fun parsesVideosAndDropsShorts() {
        val videos = RssService.parse(KXmlParser(), feed.byteInputStream(), "UC123")

        val video = videos.single()
        assertEquals("vid1", video.id)
        assertEquals("A regular video", video.title)
        assertEquals("UC123", video.channelId)
        assertEquals("https://i4.ytimg.com/vi/vid1/hqdefault.jpg", video.thumbnailUrl)
        assertEquals(
            OffsetDateTime.parse("2026-10-08T21:00:40+00:00").toInstant().toEpochMilli(),
            video.publishedAt,
        )
    }
}
