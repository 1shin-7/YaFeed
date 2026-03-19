package com.w57736e.yafeed.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM rss_sources ORDER BY `order` ASC")
    fun getAllSources(): Flow<List<RssSource>>

    @Query("SELECT * FROM rss_sources WHERE id = :id")
    suspend fun getSourceById(id: Int): RssSource?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: RssSource): Long

    @Update
    suspend fun updateSource(source: RssSource)

    @Delete
    suspend fun deleteSource(source: RssSource)

    @Query("DELETE FROM rss_sources")
    suspend fun deleteAllSources()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sources: List<RssSource>)
    
    @Query("SELECT * FROM rss_sources WHERE url = :url LIMIT 1")
    suspend fun getSourceByUrl(url: String): RssSource?
    
    @Query("DELETE FROM rss_sources WHERE url NOT IN (:urls)")
    suspend fun deleteSourcesNotIn(urls: List<String>)
    
    @Transaction
    suspend fun upsertSources(sources: List<RssSource>) {
        sources.forEach { source ->
            val existing = getSourceByUrl(source.url)
            if (existing != null) {
                // Update existing, preserving local id
                updateSource(source.copy(id = existing.id))
            } else {
                // Insert new
                insertSource(source)
            }
        }
    }
}
