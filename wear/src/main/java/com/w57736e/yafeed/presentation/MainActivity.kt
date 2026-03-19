package com.w57736e.yafeed.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prof18.rssparser.RssParser
import com.w57736e.yafeed.data.local.AppDatabase
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.presentation.screens.home.HomeScreen
import com.w57736e.yafeed.presentation.screens.home.HomeViewModel
import com.w57736e.yafeed.presentation.screens.favorites.FavoritesScreen
import com.w57736e.yafeed.presentation.screens.favorites.FavoritesViewModel
import com.w57736e.yafeed.presentation.screens.news_detail.NewsDetailScreen
import com.w57736e.yafeed.presentation.screens.news_list.NewsListScreen
import com.w57736e.yafeed.presentation.screens.news_list.NewsListViewModel
import com.w57736e.yafeed.presentation.screens.settings.SettingsScreen
import com.w57736e.yafeed.presentation.screens.debug.WearConnectionDebugScreen
import com.w57736e.yafeed.presentation.screens.debug.WearConnectionDebugViewModel
import com.w57736e.yafeed.sync.WearableDataSyncManager
import com.w57736e.yafeed.sync.WearableConnectionManager
import com.w57736e.yafeed.sync.WearableMessageManager
import com.w57736e.yafeed.sync.DataSyncListener
import com.google.android.gms.wearable.Wearable
import com.w57736e.yafeed.sync.SettingsBundle
import com.w57736e.yafeed.presentation.theme.YaFeedTheme
import com.w57736e.yafeed.image.CoilImageLoaderFactory
import com.w57736e.yafeed.utils.BrowserHelper
import com.w57736e.yafeed.utils.NotificationHelper
import com.w57736e.yafeed.utils.ScreenUtils
import com.w57736e.yafeed.workers.RssRefreshWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var dataSyncListener: DataSyncListener? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize screen width for dynamic image sizing
        ScreenUtils.initialize(this)

        // Configure global Coil ImageLoader with custom headers
        coil.Coil.setImageLoader(CoilImageLoaderFactory.create(this))

        val db = AppDatabase.getDatabase(this)
        val prefManager = PreferenceManager(this)
        val rssParser = RssParser()
        val repository = RssRepository(db.sourceDao(), db.articleDao(), db.favoriteDao(), rssParser)
        val connectionManager = WearableConnectionManager(this)
        val syncManager = WearableDataSyncManager(this, connectionManager)

        // Register DataClient listener for sync
        dataSyncListener = DataSyncListener(this, lifecycleScope)
        Wearable.getDataClient(this).addListener(dataSyncListener!!)
        Log.d("MainActivity", "DataClient listener registered")

        // Seed default source if empty and sync to Mobile
        lifecycleScope.launch {
            val currentSources = repository.getAllSources().first()
            if (currentSources.isEmpty()) {
                repository.addSource("https://www.theverge.com/rss/index.xml", "The Verge")
                repository.addSource("https://9to5google.com/feed/", "9to5Google")
                repository.addSource("https://www.ithome.com/rss", "ITHome")
            }

            // Sync sources to Mobile after seeding
            launch(kotlinx.coroutines.Dispatchers.IO) {
                delay(2000) // Wait for connection to establish
                val sources = repository.getAllSources().first()
                if (sources.isNotEmpty()) {
                    Log.d("MainActivity", "Auto-syncing ${sources.size} sources to Mobile")
                    syncManager.syncSources(sources)
                }
            }

            // Parallelize independent operations
            launch {
                val browsers = BrowserHelper.detectAvailableBrowsers(this@MainActivity)
                prefManager.setBrowserAvailable(browsers.isNotEmpty())
                if (browsers.isNotEmpty() && prefManager.browserType.first() !in browsers) {
                    prefManager.setBrowserType(browsers.first())
                }
            }

            launch {
                NotificationHelper.createNotificationChannel(this@MainActivity)
            }

            launch {
                val updateInterval = prefManager.updateInterval.first()
                val workRequest = PeriodicWorkRequestBuilder<RssRefreshWorker>(
                    updateInterval, TimeUnit.MINUTES
                ).build()
                WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork(
                    "rss_refresh",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            }

            // Initial refresh in background (non-blocking)
            launch(kotlinx.coroutines.Dispatchers.IO) {
                val sources = repository.getAllSources().first()
                val cacheSize = prefManager.maxCacheSize.first()
                sources.forEach { source ->
                    repository.fetchAndCache(source.id, cacheSize)
                }
            }
        }

        setContent {
            YaFeedApp(repository, prefManager)
        }
    }
    
    override fun onDestroy() {
        dataSyncListener?.let {
            Wearable.getDataClient(this).removeListener(it)
            Log.d("MainActivity", "DataClient listener unregistered")
        }
        super.onDestroy()
    }
}

@Composable
fun YaFeedApp(repository: RssRepository, prefManager: PreferenceManager) {
    val navController = rememberSwipeDismissableNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var debugModeEnabled by remember { mutableStateOf(false) }

    val homeViewModel = remember { HomeViewModel(repository, prefManager) }
    val newsListViewModel = remember { NewsListViewModel(repository, prefManager, context) }
    val favoritesViewModel = remember { FavoritesViewModel(repository) }

    val connectionManager = remember { WearableConnectionManager(context) }
    val syncManager = remember { WearableDataSyncManager(context, connectionManager) }
    val messageManager = remember { WearableMessageManager(context) }
    val connectionDebugViewModel = remember {
        WearConnectionDebugViewModel(connectionManager, syncManager, messageManager, prefManager, repository)
    }

    YaFeedTheme {
        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "home"
            ) {
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onSourceClick = { sourceId ->
                        navController.navigate("news_list/$sourceId")
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    onFavoritesClick = {
                        navController.navigate("favorites")
                    }
                )
            }

            composable("favorites") {
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onArticleClick = { link ->
                        val encodedLink = URLEncoder.encode(link, StandardCharsets.UTF_8.toString())
                        navController.navigate("favorite_detail/$encodedLink")
                    }
                )
            }

            composable(
                route = "favorite_detail/{link}",
                arguments = listOf(navArgument("link") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedLink = backStackEntry.arguments?.getString("link") ?: ""
                val link = URLDecoder.decode(encodedLink, StandardCharsets.UTF_8.toString())

                var favoriteArticle by remember { mutableStateOf<com.w57736e.yafeed.domain.model.FavoriteArticle?>(null) }

                LaunchedEffect(link) {
                    favoriteArticle = repository.getFavoriteByLink(link)
                }

                val fontSize by prefManager.fontSize.collectAsState(14f)
                val showImages by prefManager.showImages.collectAsState(true)
                val browserAvailable by prefManager.browserAvailable.collectAsState(false)
                val browserType by prefManager.browserType.collectAsState("webview")
                val useOriginalImagePreview by prefManager.useOriginalImagePreview.collectAsState(false)

                favoriteArticle?.let { fav ->
                    val source = com.w57736e.yafeed.domain.model.RssSource(
                        name = fav.sourceName,
                        url = fav.sourceUrl
                    )
                    NewsDetailScreen(
                        article = fav.toRssArticle(),
                        fontSize = fontSize,
                        showImages = showImages,
                        browserAvailable = browserAvailable,
                        browserType = browserType,
                        useOriginalImagePreview = useOriginalImagePreview,
                        onOpenInBrowser = { url, type ->
                            BrowserHelper.openInBrowser(context, url, type)
                        },
                        repository = repository,
                        source = source
                    )
                }
            }

            composable(
                route = "news_list/{sourceId}",
                arguments = listOf(navArgument("sourceId") { type = NavType.IntType })
            ) { backStackEntry ->
                val sourceId = backStackEntry.arguments?.getInt("sourceId") ?: 0
                NewsListScreen(
                    viewModel = newsListViewModel,
                    sourceId = sourceId,
                    onArticleClick = { index ->
                        navController.navigate("news_detail/$index")
                    }
                )
            }

            composable(
                route = "news_detail/{index}",
                arguments = listOf(navArgument("index") { type = NavType.IntType })
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getInt("index") ?: 0
                val uiState by newsListViewModel.uiState.collectAsState()
                val article = uiState.articles.getOrNull(index)
                val fontSize by prefManager.fontSize.collectAsState(14f)
                val showImages by prefManager.showImages.collectAsState(true)
                val browserAvailable by prefManager.browserAvailable.collectAsState(false)
                val browserType by prefManager.browserType.collectAsState("webview")
                val useOriginalImagePreview by prefManager.useOriginalImagePreview.collectAsState(false)

                if (article != null) {
                    NewsDetailScreen(
                        article = article,
                        fontSize = fontSize,
                        showImages = showImages,
                        browserAvailable = browserAvailable,
                        browserType = browserType,
                        useOriginalImagePreview = useOriginalImagePreview,
                        onOpenInBrowser = { url, type ->
                            BrowserHelper.openInBrowser(context, url, type)
                        },
                        repository = repository,
                        source = uiState.source
                    )
                }
            }

            composable("settings") {
                SettingsScreen(
                    onSourcesClick = { navController.navigate("settings_sources") },
                    onGeneralClick = { navController.navigate("settings_general") },
                    onUiSettingsClick = { navController.navigate("settings_ui") },
                    onDebugClick = { navController.navigate("settings_debug") },
                    onAboutClick = { navController.navigate("settings_about") },
                    debugModeEnabled = debugModeEnabled
                )
            }

            composable("settings_sources") {
                val uiState by homeViewModel.uiState.collectAsState()
                com.w57736e.yafeed.presentation.screens.settings.SourcesScreen(
                    sources = uiState.sources,
                    onAddSource = { name, url -> /* TODO */ },
                    onDeleteSource = { source ->
                        scope.launch {
                            repository.deleteSource(source)
                        }
                    },
                    onEditSource = { sourceId ->
                        navController.navigate("settings_edit_source/$sourceId")
                    },
                    onNavigateToAddSource = { navController.navigate("settings_add_source") }
                )
            }

            composable("settings_general") {
                val maxCacheSize by prefManager.maxCacheSize.collectAsState(20)
                val updateInterval by prefManager.updateInterval.collectAsState(30)
                val browserType by prefManager.browserType.collectAsState("webview")
                val browserAvailable by prefManager.browserAvailable.collectAsState(false)
                val availableBrowsers = remember { BrowserHelper.detectAvailableBrowsers(context) }
                val notificationEnabled by prefManager.notificationEnabled.collectAsState(false)
                val saveImagesOnFavorite by prefManager.saveImagesOnFavorite.collectAsState(false)

                com.w57736e.yafeed.presentation.screens.settings.GeneralSettingsScreen(
                    maxCacheSize = maxCacheSize,
                    updateInterval = updateInterval,
                    browserType = browserType,
                    browserAvailable = browserAvailable,
                    availableBrowsers = availableBrowsers,
                    notificationEnabled = notificationEnabled,
                    saveImagesOnFavorite = saveImagesOnFavorite,
                    onMaxCacheSizeChange = { size ->
                        scope.launch { prefManager.setMaxCacheSize(size) }
                    },
                    onUpdateIntervalChange = { interval ->
                        scope.launch { prefManager.setUpdateInterval(interval) }
                    },
                    onBrowserTypeChange = { type ->
                        scope.launch { prefManager.setBrowserType(type) }
                    },
                    onNotificationEnabledChange = { enabled ->
                        scope.launch { prefManager.setNotificationEnabled(enabled) }
                    },
                    onSaveImagesOnFavoriteChange = { enabled ->
                        scope.launch { prefManager.setSaveImagesOnFavorite(enabled) }
                    }
                )
            }

            composable("settings_ui") {
                val fontSize by prefManager.fontSize.collectAsState(14f)
                val showImages by prefManager.showImages.collectAsState(true)
                val useOriginalImagePreview by prefManager.useOriginalImagePreview.collectAsState(false)

                com.w57736e.yafeed.presentation.screens.settings.UiSettingsScreen(
                    showImages = showImages,
                    fontSize = fontSize,
                    useOriginalImagePreview = useOriginalImagePreview,
                    onShowImagesChange = { show ->
                        scope.launch { prefManager.setShowImages(show) }
                    },
                    onFontSizeChange = { size ->
                        scope.launch { prefManager.setFontSize(size) }
                    },
                    onUseOriginalImagePreviewChange = { enabled ->
                        scope.launch { prefManager.setUseOriginalImagePreview(enabled) }
                    }
                )
            }

            composable("settings_add_source") {
                com.w57736e.yafeed.presentation.screens.settings.AddSourceScreen(
                    onAddSource = { url, name, notificationEnabled ->
                        scope.launch {
                            val parser = RssParser()
                            val finalName = if (name.isBlank()) {
                                try {
                                    val channel = parser.getRssChannel(url)
                                    channel.title ?: "Unknown"
                                } catch (e: Exception) {
                                    "Unknown"
                                }
                            } else {
                                name
                            }
                            repository.addSource(url, finalName, notificationEnabled)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "settings_edit_source/{sourceId}",
                arguments = listOf(navArgument("sourceId") { type = NavType.IntType })
            ) { backStackEntry ->
                val sourceId = backStackEntry.arguments?.getInt("sourceId") ?: 0
                val source = remember(sourceId) {
                    derivedStateOf {
                        homeViewModel.uiState.value.sources.find { it.id == sourceId }
                    }
                }.value

                source?.let {
                    com.w57736e.yafeed.presentation.screens.settings.EditSourceScreen(
                        source = it,
                        onSave = { name, notificationEnabled ->
                            repository.updateSource(sourceId, name, notificationEnabled)
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable("settings_about") {
                com.w57736e.yafeed.presentation.screens.settings.AboutScreen(
                    onDebugModeEnabled = { debugModeEnabled = true }
                )
            }

            composable("settings_debug") {
                com.w57736e.yafeed.presentation.screens.debug.DebugMenuScreen(
                    onMarkdownTestClick = { navController.navigate("debug_markdown_test") },
                    onMarkdownAstClick = { navController.navigate("debug_markdown_ast") },
                    onNavigateToConnectionDebug = { navController.navigate("connection_debug") }
                )
            }

            composable("debug_markdown_test") {
                com.w57736e.yafeed.presentation.screens.debug.MarkdownDebugScreen()
            }

            composable("debug_markdown_ast") {
                com.w57736e.yafeed.presentation.screens.debug.MarkdownAstDebugScreen()
            }

            composable("connection_debug") {
                WearConnectionDebugScreen(viewModel = connectionDebugViewModel)
            }
        }
        }
    }
}
