package com.totoprout.torteul.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.totoprout.torteul.data.local.TorteulDatabase
import com.totoprout.torteul.data.local.entities.CategoryEntity
import com.totoprout.torteul.data.local.entities.ChannelEntity
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
    private lateinit var db: TorteulDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TorteulDatabase::class.java).build()
        categoryDao = db.categoryDao()
        channelDao = db.channelDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeCategoryAndReadInList() = runBlocking {
        val category = CategoryEntity(name = "Tech", displayOrder = 1)
        val categoryId = categoryDao.insertCategory(category)
        
        val allCategories = categoryDao.getAllCategories().first()
        assertEquals(allCategories[0].name, "Tech")
        assertEquals(allCategories[0].id, categoryId)
    }

    @Test
    @Throws(Exception::class)
    fun writeChannelAndReadByCategory() = runBlocking {
        val categoryId = categoryDao.insertCategory(CategoryEntity(name = "Tech", displayOrder = 1))
        val channel = ChannelEntity(youtubeId = "UC123", title = "TechChannel", categoryId = categoryId)
        channelDao.insertChannel(channel)

        val channels = channelDao.getChannelsByCategory(categoryId).first()
        assertEquals(channels[0].title, "TechChannel")
        assertEquals(channels[0].categoryId, categoryId)
    }
}
