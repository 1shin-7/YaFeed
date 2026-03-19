package com.w57736e.yafeed.data.repository

import android.util.Log
import com.w57736e.yafeed.data.local.SourceDao
import com.w57736e.yafeed.domain.model.RssSource
import com.w57736e.yafeed.sync.WearableDataSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Mobile 端的源仓库
 * 所有修改都直接同步到 Wear，Wear 是唯一数据源
 */
class RssSourceRepository(
    private val sourceDao: SourceDao,
    private val wearableDataSyncManager: WearableDataSyncManager
) {
    fun getAllSources(): Flow<List<RssSource>> = sourceDao.getAllSources()

    suspend fun getSourceById(id: Int): RssSource? = sourceDao.getSourceById(id)

    suspend fun addSource(source: RssSource) {
        sourceDao.insertSource(source.copy(lastModified = System.currentTimeMillis()))
        syncAllToWear()
    }

    suspend fun updateSource(source: RssSource) {
        sourceDao.updateSource(source.copy(lastModified = System.currentTimeMillis()))
        syncAllToWear()
    }

    suspend fun deleteSource(source: RssSource) {
        sourceDao.deleteSource(source)
        syncAllToWear()
    }

    suspend fun reorderSources(sources: List<RssSource>) {
        sources.forEachIndexed { index, source ->
            sourceDao.updateSource(source.copy(order = index, lastModified = System.currentTimeMillis()))
        }
        syncAllToWear()
    }

    private suspend fun syncAllToWear() {
        try {
            Log.d("RssSourceRepository", "Syncing all sources to Wear...")
            val sources = sourceDao.getAllSources().first()
            wearableDataSyncManager.syncSources(sources)
            Log.d("RssSourceRepository", "All sources synced to Wear")
        } catch (e: Exception) {
            Log.e("RssSourceRepository", "Failed to sync to Wear", e)
        }
    }
}
