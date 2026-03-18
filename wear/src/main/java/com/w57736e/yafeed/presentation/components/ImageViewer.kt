package com.w57736e.yafeed.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.w57736e.yafeed.utils.ScreenUtils
import kotlinx.coroutines.launch

@Composable
fun ImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageDimensions by remember { mutableStateOf<IntSize?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val zoomLevels = remember(imageDimensions) {
        imageDimensions?.let { (w, h) ->
            val aspectRatio = w.toFloat() / h
            when {
                aspectRatio < 0.2f -> Pair(3f, 15f)
                aspectRatio < 0.33f -> Pair(3f, 12f)
                aspectRatio < 0.9f -> Pair(2.5f, 6f)
                aspectRatio > 3f -> Pair(2.5f, 6f)
                aspectRatio > 1.1f -> Pair(2f, 5f)
                else -> Pair(2.5f, 5f)
            }
        } ?: Pair(3f, 5f)
    }

    val adaptivePadding = remember {
        if (ScreenUtils.isRoundScreen) 20.dp else 16.dp
    }

    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                scale.snapTo(1f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(adaptivePadding)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(1024)
                .build(),
            contentDescription = null,
            onState = { state ->
                if (state is AsyncImagePainter.State.Success) {
                    val size = state.painter.intrinsicSize
                    imageDimensions = IntSize(size.width.toInt(), size.height.toInt())
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() },
                        onDoubleTap = {
                            scope.launch {
                                if (scale.value > 1f) {
                                    scale.animateTo(1f, spring())
                                    offset = Offset.Zero
                                } else {
                                    val containerAspect = size.width.toFloat() / size.height
                                    val imageAspect = imageDimensions?.let { it.width.toFloat() / it.height } ?: 1f

                                    val targetScale = (if (imageAspect > containerAspect) {
                                        size.height.toFloat() / (size.width / imageAspect)
                                    } else {
                                        size.width.toFloat() / (size.height * imageAspect)
                                    } * 1.02f).coerceIn(1f, zoomLevels.second)

                                    scale.animateTo(targetScale, spring())
                                }
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scope.launch {
                            scale.snapTo((scale.value * zoom).coerceIn(1f, zoomLevels.second))
                        }

                        val newOffset = offset + pan

                        // Calculate actual rendered image size with ContentScale.Fit
                        val containerAspect = size.width.toFloat() / size.height
                        val imageAspect = imageDimensions?.let { it.width.toFloat() / it.height } ?: 1f

                        val (renderedWidth, renderedHeight) = if (imageAspect > containerAspect) {
                            // Image is wider - fits to width
                            size.width.toFloat() to size.width / imageAspect
                        } else {
                            // Image is taller - fits to height
                            size.height * imageAspect to size.height.toFloat()
                        }

                        // Calculate max offset based on rendered size
                        val maxOffsetX = ((renderedWidth * scale.value - size.width) / 2f).coerceAtLeast(0f)
                        val maxOffsetY = ((renderedHeight * scale.value - size.height) / 2f).coerceAtLeast(0f)

                        offset = Offset(
                            x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                            y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                        )

                        if (scale.value == 1f) offset = Offset.Zero
                    }
                }
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}
