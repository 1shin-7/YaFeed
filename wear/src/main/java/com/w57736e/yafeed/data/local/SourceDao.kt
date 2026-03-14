package com.w57736e.yafeed.data.local

import androidx.room.*
import com.w57736e.yafeed.domain.model.RssSource
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM rss_sources")
    fun getAllSources(): Flow<List<RssSource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: RssSource)

    @Delete
    suspend fun deleteSource(source: RssSource)

    @Update
    suspend fun updateSource(source: RssSource)

    @Query("SELECT * FROM rss_sources WHERE id = :id")
    suspend fun getSourceById(id: Int): RssSource?
}
