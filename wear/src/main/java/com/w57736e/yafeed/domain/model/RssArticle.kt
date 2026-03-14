package com.w57736e.yafeed.domain.model

data class RssArticle(
    val title: String,
    val link: String,
    val content: String?,
    val pubDate: String?,
    val imageUrl: String?,
    val author: String? = null
)
