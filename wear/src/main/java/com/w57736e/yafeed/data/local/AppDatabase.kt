package com.w57736e.yafeed.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.w57736e.yafeed.domain.model.ArticleEntity
import com.w57736e.yafeed.domain.model.FavoriteArticle
import com.w57736e.yafeed.domain.model.RssSource

@Database(entities = [RssSource::class, ArticleEntity::class, FavoriteArticle::class], version = 8, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao
    abstract fun articleDao(): ArticleDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yafeed_database"
                )
                .fallbackToDestructiveMigration(true) // For development simplicity
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
