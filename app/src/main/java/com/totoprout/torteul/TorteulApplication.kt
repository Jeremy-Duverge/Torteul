package com.totoprout.torteul

import android.app.Application
import com.totoprout.torteul.data.backup.BackupStore
import com.totoprout.torteul.data.local.TorteulDatabase
import com.totoprout.torteul.data.network.NewPipeService
import com.totoprout.torteul.data.network.createHttpClient
import com.totoprout.torteul.data.repository.YouTubeRepository
import java.io.File

class TorteulApplication : Application() {
    private val client by lazy { createHttpClient() }
    private val database by lazy { TorteulDatabase.getDatabase(this) }
    val youtubeRepository by lazy {
        YouTubeRepository(
            db = database,
            client = client,
            backupStore = BackupStore(File(filesDir, BackupStore.FILE_NAME)),
            contentResolver = contentResolver,
        )
    }

    override fun onCreate() {
        super.onCreate()
        NewPipeService.init(client)
    }
}
