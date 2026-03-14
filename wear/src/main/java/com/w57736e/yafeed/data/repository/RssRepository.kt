package com.w57736e.yafeed.data.repository

import com.prof18.rssparser.RssParser
import com.w57736e.yafeed.data.local.SourceDao
import com.w57736e.yafeed.domain.model.RssArticle
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.flow.Flow

class RssRepository(
    private val sourceDao: SourceDao,
    private val rssParser: RssParser
) {
    fun getAllSources(): Flow<List<RssSource>> = sourceDao.getAllSources()

    suspend fun addSource(url: String, name: String) {
        val source = RssSource(name = name, url = url)
        sourceDao.insertSource(source)
    }

    suspend fun deleteSource(source: RssSource) {
        sourceDao.deleteSource(source)
    }

    suspend fun fetchArticles(url: String): List<RssArticle> {
        val channel = rssParser.getRssChannel(url)
        return channel.items.map { item ->
            RssArticle(
                title = item.title ?: "No Title",
                link = item.link ?: "",
                content = item.content ?: item.description,
                pubDate = item.pubDate,
                imageUrl = item.image,
                author = item.author
            )
        }
    }

    suspend fun updateSourceInfo(source: RssSource) {
        try {
            val channel = rssParser.getRssChannel(source.url)
            val updatedSource = source.copy(
                latestTitle = channel.items.firstOrNull()?.title,
                lastUpdate = System.currentTimeMillis()
            )
            sourceDao.updateSource(updatedSource)
        } catch (e: Exception) {
            // Log or handle error
        }
    }
}
