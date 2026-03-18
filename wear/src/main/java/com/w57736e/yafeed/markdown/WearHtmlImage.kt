package com.w57736e.yafeed.markdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.w57736e.yafeed.image.ImageUrlTransformer
import com.w57736e.yafeed.utils.ScreenUtils

@Composable
fun WearHtmlImage(src: String, alt: String) {
    val context = LocalContext.current
    val transformedUrl = ImageUrlTransformer.applyThumbnail(src, ScreenUtils.getHeroImageWidth())

    AsyncImage(
        model = coil.request.ImageRequest.Builder(context)
            .data(transformedUrl)
            .build(),
        contentDescription = alt,
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
}
