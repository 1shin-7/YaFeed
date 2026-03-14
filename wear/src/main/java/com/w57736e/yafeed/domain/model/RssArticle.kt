package com.w57736e.yafeed.domain.model

data class RssArticle(
    val title: String,
    val link: String,
    val content: String?,
    val pubDate: Long?,
    val imageUrl: String?,
    val author: String? = null,
    val categories: List<String>? = null
)
