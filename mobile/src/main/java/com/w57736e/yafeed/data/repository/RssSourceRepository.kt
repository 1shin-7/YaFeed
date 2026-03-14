package com.w57736e.yafeed.data.repository

import com.w57736e.yafeed.data.local.SourceDao
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.flow.Flow

class RssSourceRepository(private val sourceDao: SourceDao) {
    fun getAllSources(): Flow<List<RssSource>> = sourceDao.getAllSources()

    suspend fun getSourceById(id: Int): RssSource? = sourceDao.getSourceById(id)

    suspend fun addSource(source: RssSource) {
        sourceDao.insertSource(source.copy(lastModified = System.currentTimeMillis()))
    }

    suspend fun updateSource(source: RssSource) {
        sourceDao.updateSource(source.copy(lastModified = System.currentTimeMillis()))
    }

    suspend fun deleteSource(source: RssSource) {
        sourceDao.deleteSource(source)
    }

    suspend fun reorderSources(sources: List<RssSource>) {
        sources.forEachIndexed { index, source ->
            sourceDao.updateSource(source.copy(order = index, lastModified = System.currentTimeMillis()))
        }
    }

    suspend fun syncSources(sources: List<RssSource>) {
        sourceDao.insertAll(sources)
    }
}
