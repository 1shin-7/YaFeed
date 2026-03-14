package com.w57736e.yafeed.data.repository

import com.prof18.rssparser.RssParser
import com.w57736e.yafeed.data.local.ArticleDao
import com.w57736e.yafeed.data.local.SourceDao
import com.w57736e.yafeed.domain.model.ArticleEntity
import com.w57736e.yafeed.domain.model.RssArticle
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URI

class RssRepository(
    private val sourceDao: SourceDao,
    private val articleDao: ArticleDao,
    private val rssParser: RssParser
) {
    fun getAllSources(): Flow<List<RssSource>> = sourceDao.getAllSources()

    suspend fun addSource(url: String, name: String, notificationEnabled: Boolean = true) {
        val faviconUrl = resolveFavicon(url)
        val source = RssSource(name = name, url = url, faviconUrl = faviconUrl, notificationEnabled = notificationEnabled)
        sourceDao.insertSource(source)
    }

    suspend fun updateSource(sourceId: Int, name: String, notificationEnabled: Boolean) {
        val source = sourceDao.getSourceById(sourceId) ?: return
        val updatedSource = source.copy(name = name, notificationEnabled = notificationEnabled)
        sourceDao.updateSource(updatedSource)
    }

    suspend fun deleteSource(source: RssSource) {
        sourceDao.deleteSource(source)
    }

    suspend fun getSourceById(id: Int): RssSource? = sourceDao.getSourceById(id)

    suspend fun syncSources(sources: List<RssSource>) {
        sourceDao.insertAll(sources)
    }

    fun getCachedArticles(sourceId: Int): Flow<List<RssArticle>> {
        return articleDao.getArticlesBySource(sourceId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun resolveFavicon(url: String): String {
        return try {
            val uri = URI(url)
            val host = uri.host ?: ""
            "https://www.google.com/s2/favicons?domain=$host&sz=64"
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun fetchAndCache(sourceId: Int, maxCacheSize: Int) {
        val source = sourceDao.getSourceById(sourceId) ?: return
        try {
            val channel = rssParser.getRssChannel(source.url)
            val articles = channel.items.map { item ->
                ArticleEntity(
                    sourceId = sourceId,
                    title = item.title ?: "No Title",
                    link = item.link ?: "",
                    content = item.content ?: item.description,
                    pubDate = DateUtils.parseToTimestamp(item.pubDate),
                    imageUrl = item.image,
                    author = item.author
                )
            }

            // Save to DB
            articleDao.insertArticles(articles)

            // Prune old articles
            articleDao.pruneArticles(sourceId, maxCacheSize)

            // Resolve favicon if missing
            val resolvedFavicon = if (source.faviconUrl.isNullOrBlank()) {
                resolveFavicon(source.url)
            } else {
                source.faviconUrl
            }

            // Update source metadata
            val latestTitle = channel.items.firstOrNull()?.title
            val updatedSource = source.copy(
                latestTitle = latestTitle,
                lastUpdate = System.currentTimeMillis(),
                faviconUrl = resolvedFavicon
            )
            sourceDao.updateSource(updatedSource)
        } catch (e: Exception) {
            // Log or handle error
        }
    }

    // Legacy method for direct fetch if needed
    suspend fun fetchArticles(url: String): List<RssArticle> {
        val channel = rssParser.getRssChannel(url)
        return channel.items.map { item ->
            RssArticle(
                title = item.title ?: "No Title",
                link = item.link ?: "",
                content = item.content ?: item.description,
                pubDate = DateUtils.parseToTimestamp(item.pubDate),
                imageUrl = item.image,
                author = item.author
            )
        }
    }
}
