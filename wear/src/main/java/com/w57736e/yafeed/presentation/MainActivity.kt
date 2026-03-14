package com.w57736e.yafeed.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import com.w57736e.yafeed.domain.model.RssSource
import com.w57736e.yafeed.presentation.screens.home.HomeScreen
import com.w57736e.yafeed.presentation.screens.home.HomeViewModel
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
        val repository = RssRepository(db.sourceDao(), rssParser)

        // Seed default source if empty
        lifecycleScope.launch {
            val currentSources = repository.getAllSources().first()
            if (currentSources.isEmpty()) {
                repository.addSource("https://www.theverge.com/rss/index.xml", "The Verge")
                repository.addSource("https://9to5google.com/feed/", "9to5Google")
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
    
    val homeViewModel = remember { HomeViewModel(repository, prefManager) }
    val newsListViewModel = remember { NewsListViewModel(repository, context) }

    YaFeedTheme {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                val uiState = homeViewModel.uiState.collectAsState().value
                HomeScreen(
                    viewModel = homeViewModel,
                    onSourceClick = { sourceId ->
                        val source = uiState.sources.find { it.id == sourceId }
                        source?.let {
                            val encodedUrl = URLEncoder.encode(it.url, StandardCharsets.UTF_8.toString())
                            navController.navigate("news_list/$encodedUrl")
                        }
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    }
                )
            }
            
            composable(
                route = "news_list/{url}",
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url") ?: ""
                NewsListScreen(
                    viewModel = newsListViewModel,
                    url = url,
                    onArticleClick = { index ->
                        // Navigate to detail
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onSourcesClick = { navController.navigate("settings_sources") },
                    onUiSettingsClick = { navController.navigate("settings_ui") },
                    onAboutClick = { navController.navigate("settings_about") }
                )
            }

            composable("settings_sources") {
                val sources = homeViewModel.uiState.collectAsState().value.sources
                com.w57736e.yafeed.presentation.screens.settings.SourcesScreen(
                    sources = sources,
                    onAddSource = { name, url -> /* TODO */ },
                    onDeleteSource = { source -> /* TODO */ }
                )
            }

            composable("settings_ui") {
                val uiState = homeViewModel.uiState.collectAsState().value
                com.w57736e.yafeed.presentation.screens.settings.UiSettingsScreen(
                    uiScale = uiState.uiScale,
                    showImages = true,
                    updateInterval = 30,
                    onUiScaleChange = { /* TODO */ },
                    onShowImagesChange = { /* TODO */ },
                    onUpdateIntervalChange = { /* TODO */ }
                )
            }

            composable("settings_about") {
                com.w57736e.yafeed.presentation.screens.settings.AboutScreen()
            }
        }
    }
}
