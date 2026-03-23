package com.w57736e.yafeed.data.repository

import android.util.Log
import com.prof18.rssparser.RssParser
import com.w57736e.yafeed.data.local.ArticleDao
import com.w57736e.yafeed.data.local.FavoriteDao
import com.w57736e.yafeed.data.local.SourceDao
import com.w57736e.yafeed.domain.model.ArticleEntity
import com.w57736e.yafeed.domain.model.FavoriteArticle
import com.w57736e.yafeed.domain.model.RssArticle
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.net.URI

class RssRepository(
    private val sourceDao: SourceDao,
    private val articleDao: ArticleDao,
    private val favoriteDao: FavoriteDao,
    private val rssParser: RssParser
) {
    companion object {
        private const val TAG = "RssRepository"
    }
    fun getAllSources(): Flow<List<RssSource>> = sourceDao.getAllSources()

    suspend fun addSource(url: String, name: String, notificationEnabled: Boolean = true) {
        val faviconUrl = resolveFavicon(url)
        val source = RssSource(name = name, url = url, faviconUrl = faviconUrl, notificationEnabled = notificationEnabled, lastModified = System.currentTimeMillis())
        sourceDao.insertSource(source)
    }

    suspend fun updateSource(sourceId: Int, name: String, notificationEnabled: Boolean) {
        val source = sourceDao.getSourceById(sourceId) ?: return
        val updatedSource = source.copy(name = name, notificationEnabled = notificationEnabled, lastModified = System.currentTimeMillis())
        sourceDao.updateSource(updatedSource)
    }

    suspend fun deleteSource(source: RssSource) {
        sourceDao.deleteSource(source)
    }

    suspend fun getSourceById(id: Int): RssSource? = sourceDao.getSourceById(id)

    suspend fun syncSources(sources: List<RssSource>) {
        sourceDao.upsertSources(sources)
        // 删除本地不存在于远程列表中的源
        val remoteUrls = sources.map { it.url }
        if (remoteUrls.isNotEmpty()) {
            sourceDao.deleteSourcesNotIn(remoteUrls)
        } else {
            // 如果远程列表为空，删除所有本地源
            val localSources = getAllSources().first()
            localSources.forEach { sourceDao.deleteSource(it) }
        }
    }

    fun getCachedArticles(sourceId: Int): Flow<List<RssArticle>> {
        return articleDao.getArticlesBySource(sourceId)
            .distinctUntilChanged()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    suspend fun getArticlesForDisplay(sourceId: Int, isOnline: Boolean): List<RssArticle> {
        return if (isOnline) {
            try {
                val source = sourceDao.getSourceById(sourceId) ?: return getCachedArticles(sourceId).map { it }.first()
                val channel = rssParser.getRssChannel(source.url)
                channel.items.map { item ->
                    val title = item.title ?: "No Title"
                    val timestamp = DateUtils.parseToTimestamp(item.pubDate)
                    RssArticle(
                        title = title,
                        link = item.link ?: "",
                        content = item.content ?: item.description,
                        pubDate = timestamp,
                        imageUrl = item.image,
                        author = item.author,
                        cleanTitle = cleanHtmlEntities(title),
                        formattedDate = DateUtils.formatRssDate(timestamp)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch articles for source $sourceId: ${e.message}", e)
                getCachedArticles(sourceId).map { it }.first()
            }
        } else {
            getCachedArticles(sourceId).map { it }.first()
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
                    author = item.author,
                    fetchedAt = System.currentTimeMillis()
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
                faviconUrl = resolvedFavicon,
                lastModified = System.currentTimeMillis()
            )
            sourceDao.updateSource(updatedSource)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch and cache for source ${source.url}: ${e.message}", e)
        }
    }

    // Legacy method for direct fetch if needed
    suspend fun fetchArticles(url: String): List<RssArticle> {
        return try {
            val channel = rssParser.getRssChannel(url)
            channel.items.map { item ->
                val title = item.title ?: "No Title"
                val timestamp = DateUtils.parseToTimestamp(item.pubDate)
                RssArticle(
                    title = title,
                    link = item.link ?: "",
                    content = item.content ?: item.description,
                    pubDate = timestamp,
                    imageUrl = item.image,
                    author = item.author,
                    cleanTitle = cleanHtmlEntities(title),
                    formattedDate = DateUtils.formatRssDate(timestamp)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch articles from $url: ${e.message}", e)
            emptyList()
        }
    }

    private fun cleanHtmlEntities(text: String): String {
        var result = text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")

        val numericEntityRegex = Regex("&#(\\d+);")
        result = numericEntityRegex.replace(result) { matchResult ->
            val code = matchResult.groupValues[1].toIntOrNull()
            if (code != null && code in 1..0x10FFFF) {
                code.toChar().toString()
            } else {
                matchResult.value
            }
        }

        return result
    }

    // Favorites
    suspend fun addFavorite(article: RssArticle, source: RssSource) {
        val favorite = FavoriteArticle(
            title = article.title,
            link = article.link,
            content = article.content,
            pubDate = article.pubDate,
            imageUrl = article.imageUrl,
            author = article.author,
            sourceName = source.name,
            sourceUrl = source.url,
            savedAt = System.currentTimeMillis()
        )
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun deleteFavorite(favorite: FavoriteArticle) {
        favoriteDao.deleteFavorite(favorite)
    }

    suspend fun toggleFavorite(article: RssArticle, source: RssSource) {
        val existing = favoriteDao.getFavoriteByLink(article.link)
        if (existing != null) {
            favoriteDao.deleteFavorite(existing)
        } else {
            addFavorite(article, source)
        }
    }

    fun getAllFavorites(): Flow<List<FavoriteArticle>> = favoriteDao.getAllFavorites()
        .distinctUntilChanged()

    fun isFavorite(link: String): Flow<Boolean> = favoriteDao.isFavorite(link)

    suspend fun getFavoriteByLink(link: String): FavoriteArticle? = favoriteDao.getFavoriteByLink(link)
}
