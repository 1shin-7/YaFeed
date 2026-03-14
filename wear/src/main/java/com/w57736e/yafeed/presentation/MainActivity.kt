package com.w57736e.yafeed.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.prof18.rssparser.RssParser
import com.w57736e.yafeed.data.local.AppDatabase
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssRepository
import com.w57736e.yafeed.presentation.screens.home.HomeScreen
import com.w57736e.yafeed.presentation.screens.home.HomeViewModel
import com.w57736e.yafeed.presentation.screens.news_detail.NewsDetailScreen
import com.w57736e.yafeed.presentation.screens.news_list.NewsListScreen
import com.w57736e.yafeed.presentation.screens.news_list.NewsListViewModel
import com.w57736e.yafeed.presentation.screens.settings.SettingsScreen
import com.w57736e.yafeed.presentation.theme.YaFeedTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        val prefManager = PreferenceManager(this)
        val rssParser = RssParser()
        val repository = RssRepository(db.sourceDao(), db.articleDao(), rssParser)

        // Seed default source if empty
        lifecycleScope.launch {
            val currentSources = repository.getAllSources().first()
            if (currentSources.isEmpty()) {
                repository.addSource("https://www.theverge.com/rss/index.xml", "The Verge")
                repository.addSource("https://9to5google.com/feed/", "9to5Google")
                repository.addSource("https://www.ithome.com/rss", "ITHome")
            }
        }

        setContent {
            YaFeedApp(repository, prefManager)
        }
    }
}

@Composable
fun YaFeedApp(repository: RssRepository, prefManager: PreferenceManager) {
    val navController = rememberSwipeDismissableNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val homeViewModel = remember { HomeViewModel(repository, prefManager) }
    val newsListViewModel = remember { NewsListViewModel(repository, prefManager, context) }

    YaFeedTheme {
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
                    }
                )
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

                if (article != null) {
                    NewsDetailScreen(article = article, fontSize = fontSize, showImages = showImages)
                }
            }

            composable("settings") {
                SettingsScreen(
                    onSourcesClick = { navController.navigate("settings_sources") },
                    onGeneralClick = { navController.navigate("settings_general") },
                    onUiSettingsClick = { navController.navigate("settings_ui") },
                    onAboutClick = { navController.navigate("settings_about") }
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
                    onNavigateToAddSource = { navController.navigate("settings_add_source") }
                )
            }

            composable("settings_general") {
                val maxCacheSize by prefManager.maxCacheSize.collectAsState(20)

                com.w57736e.yafeed.presentation.screens.settings.GeneralSettingsScreen(
                    maxCacheSize = maxCacheSize,
                    updateInterval = 30,
                    onMaxCacheSizeChange = { size ->
                        scope.launch { prefManager.setMaxCacheSize(size) }
                    },
                    onUpdateIntervalChange = { /* TODO */ }
                )
            }

            composable("settings_ui") {
                val fontSize by prefManager.fontSize.collectAsState(14f)
                val showImages by prefManager.showImages.collectAsState(true)

                com.w57736e.yafeed.presentation.screens.settings.UiSettingsScreen(
                    showImages = showImages,
                    fontSize = fontSize,
                    onShowImagesChange = { show ->
                        scope.launch { prefManager.setShowImages(show) }
                    },
                    onFontSizeChange = { size ->
                        scope.launch { prefManager.setFontSize(size) }
                    }
                )
            }

            composable("settings_add_source") {
                com.w57736e.yafeed.presentation.screens.settings.AddSourceScreen(
                    onAddSource = { url, name ->
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
                            repository.addSource(url, finalName)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings_about") {
                com.w57736e.yafeed.presentation.screens.settings.AboutScreen()
            }
        }
    }
}
