package com.w57736e.yafeed.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}
