package com.w57736e.yafeed.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.w57736e.yafeed.domain.model.FavoriteArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteArticle)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteArticle)

    @Query("SELECT * FROM favorite_articles ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteArticle>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_articles WHERE link = :link)")
    fun isFavorite(link: String): Flow<Boolean>

    @Query("SELECT * FROM favorite_articles WHERE link = :link LIMIT 1")
    suspend fun getFavoriteByLink(link: String): FavoriteArticle?
}
