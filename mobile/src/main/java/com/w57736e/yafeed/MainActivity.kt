package com.w57736e.yafeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.w57736e.yafeed.data.local.AppDatabase
import com.w57736e.yafeed.data.local.PreferenceManager
import com.w57736e.yafeed.data.repository.RssSourceRepository
import com.w57736e.yafeed.data.repository.SettingsRepository
import com.w57736e.yafeed.domain.model.RssSource
import com.w57736e.yafeed.sync.WearConnectionManager
import com.w57736e.yafeed.ui.components.WearDisconnectedOverlay
import com.w57736e.yafeed.ui.sources.AddEditSourceScreen
import com.w57736e.yafeed.ui.sources.SourcesScreen
import com.w57736e.yafeed.ui.sources.SourcesViewModel
import com.w57736e.yafeed.ui.settings.SettingsScreen
import com.w57736e.yafeed.ui.settings.SettingsViewModel
import com.w57736e.yafeed.ui.theme.YaFeedTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val preferenceManager = PreferenceManager(this)
        val sourcesViewModel = SourcesViewModel(RssSourceRepository(database.sourceDao()))
        val settingsViewModel = SettingsViewModel(SettingsRepository(preferenceManager))
        val connectionManager = WearConnectionManager(this)

        lifecycleScope.launch {
            while (isActive) {
                connectionManager.checkConnection()
                delay(5000)
            }
        }

        setContent {
            YaFeedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isConnected by connectionManager.isConnected.collectAsState()
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Sources) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val screen = currentScreen) {
                            Screen.Sources -> SourcesScreen(
                                viewModel = sourcesViewModel,
                                onNavigateToSettings = { currentScreen = Screen.Settings },
                                onNavigateToAddEdit = { currentScreen = Screen.AddEdit(it) }
                            )
                            Screen.Settings -> SettingsScreen(
                                viewModel = settingsViewModel,
                                onNavigateBack = { currentScreen = Screen.Sources }
                            )
                            is Screen.AddEdit -> AddEditSourceScreen(
                                source = screen.source,
                                viewModel = sourcesViewModel,
                                onNavigateBack = { currentScreen = Screen.Sources }
                            )
                        }

                        if (!isConnected) {
                            WearDisconnectedOverlay()
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Sources : Screen()
    object Settings : Screen()
    data class AddEdit(val source: RssSource?) : Screen()
}
