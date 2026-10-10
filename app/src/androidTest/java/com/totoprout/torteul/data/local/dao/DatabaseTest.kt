package com.totoprout.torteul.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.totoprout.torteul.data.local.TorteulDatabase
import com.totoprout.torteul.data.local.entities.CategoryEntity
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.data.local.entities.VideoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var categoryDao: CategoryDao
    private lateinit var channelDao: ChannelDao
    private lateinit var videoDao: VideoDao
    private lateinit var db: TorteulDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TorteulDatabase::class.java).build()
        categoryDao = db.categoryDao()
        channelDao = db.channelDao()
        videoDao = db.videoDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeCategoryAndReadInList() = runBlocking {
        categoryDao.insertCategory(CategoryEntity(name = "Tech"))

        val allCategories = categoryDao.getAllCategories().first()
        assertEquals(listOf("Tech"), allCategories.map { it.name })
    }

    @Test
    @Throws(Exception::class)
    fun feedKeepsOnlySelectedCategories() = runBlocking {
        channelDao.insertChannel(ChannelEntity(id = "UC1", title = "TechChannel", category = "Tech"))
        channelDao.insertChannel(ChannelEntity(id = "UC2", title = "Other"))
        videoDao.upsertVideos(
            listOf(
                VideoEntity("v1", "Tech video", 2_000L, "UC1", ""),
                VideoEntity("v2", "Other video", 1_000L, "UC2", ""),
            )
        )

        val feed = videoDao.getFeedForCategories(listOf("Tech")).first()
        assertEquals(listOf("v1"), feed.map { it.video.id })
        assertEquals("TechChannel", feed.single().channelTitle)
    }
}
