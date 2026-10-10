package com.totoprout.torteul.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.room.withTransaction
import com.totoprout.torteul.data.backup.BackupStore
import com.totoprout.torteul.data.backup.SubscriptionBackup
import com.totoprout.torteul.data.local.FeedItem
import com.totoprout.torteul.data.local.TorteulDatabase
import com.totoprout.torteul.data.local.entities.CategoryEntity
import com.totoprout.torteul.data.local.entities.ChannelEntity
import com.totoprout.torteul.data.network.NewPipeService
import com.totoprout.torteul.data.network.RssService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.IOException

private const val TAG = "YouTubeRepository"
private const val REFRESH_INTERVAL_MS = 15 * 60 * 1000L

class YouTubeRepository(
    private val db: TorteulDatabase,
    private val client: OkHttpClient,
    private val backupStore: BackupStore,
    private val contentResolver: ContentResolver,
) {
    private val categoryDao = db.categoryDao()
    private val channelDao = db.channelDao()
    private val videoDao = db.videoDao()
    private val backupMutex = Mutex()

    @Volatile
    private var lastRefreshAt: Long? = null

    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val channels: Flow<List<ChannelEntity>> = channelDao.getAllChannels()

    fun feed(categories: Set<String>): Flow<List<FeedItem>> =
        if (categories.isEmpty()) videoDao.getFeed()
        else videoDao.getFeedForCategories(categories.toList())

    fun isRefreshDue(): Boolean {
        val last = lastRefreshAt ?: return true
        return SystemClock.elapsedRealtime() - last >= REFRESH_INTERVAL_MS
    }

    /** Returns the titles of the channels whose feed couldn't be fetched. */
    suspend fun refreshVideos(): List<String> {
        val failed = mutableListOf<String>()
        for (channel in channelDao.getAllChannels().first()) {
            try {
                fetchVideos(channel.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Refresh failed for ${channel.title}", e)
                failed += channel.title
            }
        }
        lastRefreshAt = SystemClock.elapsedRealtime()
        return failed
    }

    /** Accepts @handle, handle, channel id (UC…) or URL; returns the channel name. */
    suspend fun addChannel(input: String): String {
        val trimmed = input.trim()
        val url = when {
            trimmed.startsWith("http") -> trimmed
            trimmed.startsWith("UC") && trimmed.length == 24 -> "https://www.youtube.com/channel/$trimmed"
            else -> "https://www.youtube.com/@${trimmed.removePrefix("@")}"
        }
        val info = NewPipeService.getChannelInfo(url)
        channelDao.insertChannel(ChannelEntity(id = info.id, title = info.name))
        saveBackup()
        try {
            fetchVideos(info.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "First fetch failed for ${info.name}", e)
        }
        return info.name
    }

    suspend fun unsubscribe(channel: ChannelEntity) {
        db.withTransaction {
            videoDao.deleteByChannel(channel.id)
            channelDao.deleteChannel(channel)
        }
        saveBackup()
    }

    suspend fun setCategory(channel: ChannelEntity, category: String) {
        channelDao.setCategory(channel.id, category)
        saveBackup()
    }

    suspend fun addCategory(name: String) {
        categoryDao.insertCategory(CategoryEntity(name.trim()))
        saveBackup()
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        db.withTransaction {
            channelDao.clearCategory(category.name)
            categoryDao.deleteCategory(category)
        }
        saveBackup()
    }

    /** Returns the number of exported channels. */
    suspend fun exportTo(uri: Uri): Int {
        val backup = currentBackup()
        withContext(Dispatchers.IO) {
            val out = contentResolver.openOutputStream(uri, "wt") ?: throw IOException("Can't write $uri")
            out.use { it.write(BackupStore.encode(backup)) }
        }
        return backup.channels.size
    }

    /** Merges the file into the current subscriptions; returns the number of imported channels. */
    suspend fun importFrom(uri: Uri): Int {
        val backup = withContext(Dispatchers.IO) {
            val input = contentResolver.openInputStream(uri) ?: throw IOException("Can't read $uri")
            BackupStore.decode(input.use { it.readBytes() })
        }
        restore(backup)
        saveBackup()
        return backup.channels.size
    }

    /** After a destructive migration (or a fresh install restored by Android), rebuilds subscriptions from the backup file. */
    suspend fun restoreIfEmpty() {
        try {
            if (channelDao.count() > 0) return
            val backup = withContext(Dispatchers.IO) { backupStore.load() } ?: return
            restore(backup)
            Log.i(TAG, "Restored ${backup.channels.size} channels from ${BackupStore.FILE_NAME}")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Restore from ${BackupStore.FILE_NAME} failed", e)
        }
    }

    private suspend fun restore(backup: SubscriptionBackup) {
        val categoryNames = (backup.categories + backup.channels.map { it.category })
            .filter { it.isNotBlank() }
            .distinct()
        db.withTransaction {
            categoryDao.insertCategories(categoryNames.map(::CategoryEntity))
            channelDao.upsertChannels(backup.channels.map { ChannelEntity(it.id, it.title, it.category) })
        }
    }

    private suspend fun fetchVideos(channelId: String) {
        videoDao.upsertVideos(RssService.fetchVideos(client, channelId))
    }

    private suspend fun currentBackup() = SubscriptionBackup(
        categories = categoryDao.getAllCategories().first().map { it.name },
        channels = channelDao.getAllChannels().first().map {
            SubscriptionBackup.Channel(it.id, it.title, it.category)
        },
    )

    private suspend fun saveBackup() {
        try {
            backupMutex.withLock {
                val backup = currentBackup()
                withContext(Dispatchers.IO) { backupStore.save(backup) }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Couldn't write ${BackupStore.FILE_NAME}", e)
        }
    }
}
