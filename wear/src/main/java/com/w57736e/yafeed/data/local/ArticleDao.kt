package com.w57736e.yafeed.data.local

import androidx.room.*
import com.w57736e.yafeed.domain.model.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("SELECT * FROM cached_articles WHERE sourceId = :sourceId ORDER BY pubDate DESC")
    fun getArticlesBySource(sourceId: Int): Flow<List<ArticleEntity>>

    @Query("DELETE FROM cached_articles WHERE sourceId = :sourceId AND id NOT IN (SELECT id FROM cached_articles WHERE sourceId = :sourceId ORDER BY pubDate DESC LIMIT :limit)")
    suspend fun pruneArticles(sourceId: Int, limit: Int)

    @Query("DELETE FROM cached_articles WHERE sourceId = :sourceId")
    suspend fun deleteArticlesBySource(sourceId: Int)
}
