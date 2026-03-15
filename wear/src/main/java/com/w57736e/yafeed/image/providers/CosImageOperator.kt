package com.w57736e.yafeed.image.providers

import android.net.Uri
import com.w57736e.yafeed.image.ImageOperator

class CosImageOperator(private val baseUrl: String) : ImageOperator {
    private var width: Int? = null
    private var height: Int? = null
    private var formatValue: String? = null

    override fun resize(width: Int, height: Int): ImageOperator {
        this.width = width
        this.height = height
        return this
    }

    override fun format(format: String): ImageOperator {
        this.formatValue = format
        return this
    }

    override fun build(): String {
        val operations = mutableListOf<String>()

        if (width != null && height != null) {
            operations.add("thumbnail/${width}x${height}")
        }

        if (formatValue != null) {
            operations.add("format/$formatValue")
        }

        if (operations.isEmpty()) return baseUrl

        val paramString = "imageMogr2/${operations.joinToString("/")}"
        return appendQueryParam(baseUrl, paramString)
    }

    private fun appendQueryParam(url: String, paramString: String): String {
        val uri = Uri.parse(url)
        return if (uri.query.isNullOrEmpty()) {
            "$url?$paramString"
        } else {
            "$url&$paramString"
        }
    }

    companion object {
        private val pattern = Regex(".*\\.myqcloud\\.com.*")
        fun matches(url: String) = pattern.matches(url)
        fun create(url: String) = CosImageOperator(url)
    }
}
