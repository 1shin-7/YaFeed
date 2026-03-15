package com.w57736e.yafeed.image

object ImageUrlTransformer {
    fun applyThumbnail(url: String?, width: Int): String? {
        if (url == null) return null
        val operator = ImageOperatorFactory.create(url) ?: return url
        return operator.resize(width, width).format("webp").build()
    }
}
