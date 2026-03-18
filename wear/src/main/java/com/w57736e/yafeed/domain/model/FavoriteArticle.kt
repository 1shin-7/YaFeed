package com.w57736e.yafeed.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_articles",
    indices = [Index(value = ["link"], unique = true)]
)
data class FavoriteArticle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val link: String,
    val content: String?,
    val pubDate: Long?,
    val imageUrl: String?,
    val author: String?,
    val sourceName: String,
    val sourceUrl: String,
    val savedAt: Long = System.currentTimeMillis(),
    val localImagePath: String? = null
) {
    fun toRssArticle() = RssArticle(
        title = title,
        link = link,
        content = content,
        pubDate = pubDate,
        imageUrl = imageUrl,
        author = author
    )
}
