package com.w57736e.yafeed.image

interface ImageOperator {
    fun resize(width: Int, height: Int): ImageOperator
    fun format(format: String): ImageOperator
    fun build(): String
}
