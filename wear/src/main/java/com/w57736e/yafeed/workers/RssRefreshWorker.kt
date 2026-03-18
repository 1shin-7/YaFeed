package com.w57736e.yafeed.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prof18.rssparser.RssParser
import com.w57736e.yafeed.data.local.AppDatabase
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.utils.NotificationHelper
import kotlinx.coroutines.flow.first

class RssRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val prefManager = PreferenceManager(applicationContext)
            val repository = RssRepository(db.sourceDao(), db.articleDao(), db.favoriteDao(), RssParser())

            val notificationEnabled = prefManager.notificationEnabled.first()
            if (!notificationEnabled) {
                return Result.success()
            }

            val sources = repository.getAllSources().first()
            val cacheSize = prefManager.maxCacheSize.first()

            sources.forEach { source ->
                if (!source.notificationEnabled) return@forEach

                val oldArticles = repository.getCachedArticles(source.id).first()
                repository.fetchAndCache(source.id, cacheSize)
                val newArticles = repository.getCachedArticles(source.id).first()

                val newItems = newArticles.filterNot { new ->
                    oldArticles.any { old -> old.link == new.link }
                }

                if (newItems.isNotEmpty()) {
                    NotificationHelper.sendNewArticlesNotification(
                        applicationContext,
                        newItems,
                        source.name
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
