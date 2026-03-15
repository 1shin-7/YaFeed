package com.w57736e.yafeed.data.repository

import android.util.Log
import com.w57736e.yafeed.data.local.SourceDao
import com.w57736e.yafeed.domain.model.RssSource
import com.w57736e.yafeed.sync.WearSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RssSourceRepository(
    private val sourceDao: SourceDao,
    private val wearSyncManager: WearSyncManager
) {
    fun getAllSources(): Flow<List<RssSource>> = sourceDao.getAllSources()

    suspend fun getSourceById(id: Int): RssSource? = sourceDao.getSourceById(id)

    suspend fun addSource(source: RssSource) {
        sourceDao.insertSource(source.copy(lastModified = System.currentTimeMillis()))
        syncToWear()
    }

    suspend fun updateSource(source: RssSource) {
        sourceDao.updateSource(source.copy(lastModified = System.currentTimeMillis()))
        syncToWear()
    }

    suspend fun deleteSource(source: RssSource) {
        sourceDao.deleteSource(source)
        syncToWear()
    }

    suspend fun reorderSources(sources: List<RssSource>) {
        sources.forEachIndexed { index, source ->
            sourceDao.updateSource(source.copy(order = index, lastModified = System.currentTimeMillis()))
        }
        syncToWear()
    }

    suspend fun syncSources(sources: List<RssSource>) {
        sourceDao.insertAll(sources)
    }

    private suspend fun syncToWear() {
        try {
            Log.d("SourceSync", "Syncing sources to Wear...")
            val sources = sourceDao.getAllSources().first()
            wearSyncManager.syncSources(sources)
            Log.d("SourceSync", "Sources synced successfully")
        } catch (e: Exception) {
            Log.e("SourceSync", "Failed to sync sources", e)
        }
    }
}
