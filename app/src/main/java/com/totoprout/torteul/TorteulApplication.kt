package com.totoprout.torteul

import android.app.Application
import com.totoprout.torteul.data.local.TorteulDatabase
import com.totoprout.torteul.data.network.NewPipeService
import com.totoprout.torteul.data.repository.YouTubeRepository
import okhttp3.OkHttpClient

class TorteulApplication : Application() {
    private val client by lazy { OkHttpClient() }
    private val database by lazy { TorteulDatabase.getDatabase(this) }
    val youtubeRepository by lazy { YouTubeRepository(database, client) }

    override fun onCreate() {
        super.onCreate()
        NewPipeService.init(client)
    }
}
