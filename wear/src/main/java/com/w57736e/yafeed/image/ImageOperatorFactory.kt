package com.w57736e.yafeed.image

import com.w57736e.yafeed.image.providers.BceImageOperator
import com.w57736e.yafeed.image.providers.CosImageOperator
import com.w57736e.yafeed.image.providers.OssImageOperator

object ImageOperatorFactory {
    fun create(url: String): ImageOperator? {
        return when {
            OssImageOperator.matches(url) -> OssImageOperator.create(url)
            CosImageOperator.matches(url) -> CosImageOperator.create(url)
            BceImageOperator.matches(url) -> BceImageOperator.create(url)
            else -> null
        }
    }
}
