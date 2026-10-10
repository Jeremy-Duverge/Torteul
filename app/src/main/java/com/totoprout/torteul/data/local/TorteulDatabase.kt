package com.totoprout.torteul.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.totoprout.torteul.data.local.dao.*
import com.totoprout.torteul.data.local.entities.*

@Database(
    entities = [
        CategoryEntity::class,
        ChannelEntity::class,
        VideoEntity::class,
        RuleEntity::class
    ],
    // Bumping the version wipes the DB; YouTubeRepository.restoreIfEmpty() brings subscriptions back.
    version = 2,
    exportSchema = false
)
abstract class TorteulDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun channelDao(): ChannelDao
    abstract fun videoDao(): VideoDao
    abstract fun ruleDao(): RuleDao

    companion object {
        @Volatile
        private var INSTANCE: TorteulDatabase? = null

        fun getDatabase(context: Context): TorteulDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TorteulDatabase::class.java,
                    "torteul_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
