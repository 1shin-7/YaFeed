package com.w57736e.yafeed.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rss_sources")
data class RssSource(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val url: String,
    val faviconUrl: String? = null,
    val lastUpdate: Long = 0,
    val latestTitle: String? = null,
    val notificationEnabled: Boolean = true
)
