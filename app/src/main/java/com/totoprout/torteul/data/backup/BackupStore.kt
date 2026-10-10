package com.totoprout.torteul.data.backup

import android.util.AtomicFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@Serializable
data class SubscriptionBackup(
    val version: Int = 1,
    val categories: List<String> = emptyList(),
    val channels: List<Channel> = emptyList(),
) {
    @Serializable
    data class Channel(val id: String, val title: String, val category: String = "")
}

class BackupStore(file: File) {
    private val file = AtomicFile(file)

    fun load(): SubscriptionBackup? = try {
        decode(file.readFully())
    } catch (e: FileNotFoundException) {
        null
    }

    fun save(backup: SubscriptionBackup) {
        val out = file.startWrite()
        try {
            out.write(encode(backup))
            file.finishWrite(out)
        } catch (e: IOException) {
            file.failWrite(out)
            throw e
        }
    }

    companion object {
        const val FILE_NAME = "subscriptions.json"

        private val json = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        fun encode(backup: SubscriptionBackup): ByteArray =
            json.encodeToString(SubscriptionBackup.serializer(), backup).toByteArray()

        fun decode(bytes: ByteArray): SubscriptionBackup =
            json.decodeFromString(SubscriptionBackup.serializer(), bytes.decodeToString())
    }
}
