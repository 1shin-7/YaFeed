package com.w57736e.yafeed.presentation.screens.news_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import coil.compose.SubcomposeAsyncImage
import com.w57736e.yafeed.R
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.domain.model.RssArticle
import com.w57736e.yafeed.domain.model.RssSource
import com.w57736e.yafeed.data.repository.DateUtils
import com.w57736e.yafeed.presentation.components.ImageViewer
import com.w57736e.yafeed.presentation.components.ContentRenderer
import com.w57736e.yafeed.presentation.components.ParticleBox
import com.w57736e.yafeed.image.ImageUrlTransformer
import com.w57736e.yafeed.utils.ScreenUtils
import kotlinx.coroutines.launch

@Composable
fun NewsDetailScreen(
    article: RssArticle,
    fontSize: Float = 1.0f,
    showImages: Boolean = true,
    browserAvailable: Boolean = false,
    browserType: String = "webview",
    useOriginalImagePreview: Boolean = false,
    onOpenInBrowser: (String, String) -> Unit = { _, _ -> },
    repository: RssRepository? = null,
    source: RssSource? = null
) {
    val scrollState = rememberTransformingLazyColumnState()
    val formattedDate = remember(article.pubDate) { DateUtils.formatRssDateFull(article.pubDate) }
    val content = article.content ?: stringResource(R.string.no_content_available)
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showNoBrowserDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isFavorited by remember(article.link, repository) {
        repository?.isFavorite(article.link) ?: kotlinx.coroutines.flow.flowOf(false)
    }.collectAsState(initial = false)

    selectedImageUrl?.let { imageUrl ->
        Dialog(onDismissRequest = { selectedImageUrl = null }) {
            ImageViewer(
                imageUrl = imageUrl,
                onDismiss = { selectedImageUrl = null }
            )
        }
    }

    if (showNoBrowserDialog) {
        Dialog(onDismissRequest = { showNoBrowserDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.no_browser_available),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.cannot_open),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = { showNoBrowserDialog = false }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }

    ScreenScaffold(
            scrollState = scrollState,
            edgeButton = {
            EdgeButton(
                onClick = {
                    if (browserAvailable) {
                        article.link?.let { onOpenInBrowser(it, browserType) }
                    } else {
                        showNoBrowserDialog = true
                    }
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        stringResource(R.string.view),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    ) { contentPadding ->
        TransformingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 0.dp,
                start = 0.dp,
                end = 0.dp,
                bottom = contentPadding.calculateBottomPadding()
            )
        ) {
            // 顶部图片 + 渐变遮罩 + 标题 复合项
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // 1. 底层图片
                    if (article.imageUrl != null && showImages) {
                        SubcomposeAsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(ImageUrlTransformer.applyThumbnail(article.imageUrl, ScreenUtils.getHeroImageWidth()))
                                .size(coil.size.Size(400, 400))
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                ParticleBox(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    } else {
                        // 无图时的保底背景色
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer))
                    }

                    // 2. 渐变遮罩 (从透明到背景色)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Black
                                    ),
                                    startY = 100f
                                )
                            )
                    )

                    // 3. 标题 (定位在底部)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    blurRadius = 8f
                                )
                            )
                        )
                    }
                }
            }

            // 作者和日期 (添加水平 Padding)
            if (article.author != null || article.pubDate != null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        article.author?.let { author ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    author,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        formattedDate?.let { date ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        source?.let { src ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.RssFeed,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    src.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        article.categories?.takeIf { it.isNotEmpty() }?.let { categories ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                categories.take(3).forEach { category ->
                                    Box(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 正文 (添加水平 Padding)
            item {
                ContentRenderer(
                    content = content,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.2f).sp
                    ),
                    onImageClick = { url -> selectedImageUrl = url },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    showImages = showImages
                )
            }

            // Favorite button
            if (repository != null && source != null) {
                item {
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                repository.toggleFavorite(article, source)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                    ) {
                        Icon(
                            if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isFavorited) "Unfavorite" else "Favorite")
                    }
                }
            }
        }
    }
}
