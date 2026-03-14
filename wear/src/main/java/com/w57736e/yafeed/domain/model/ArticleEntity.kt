package com.w57736e.yafeed.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_articles",
    foreignKeys = [
        ForeignKey(
            entity = RssSource::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sourceId"])]
)
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sourceId: Int,
    val title: String,
    val link: String,
    val content: String?,
    val pubDate: String?,
    val imageUrl: String?,
    val author: String?,
    val fetchedAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = RssArticle(
        title = title,
        link = link,
        content = content,
        pubDate = pubDate,
        imageUrl = imageUrl,
        author = author
    )
}
