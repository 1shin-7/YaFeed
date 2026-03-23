package com.w57736e.yafeed

import android.os.Bundle
import android.util.Log
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
import com.w57736e.yafeed.sync.WearableConnectionManager
import com.w57736e.yafeed.sync.WearableDataSyncManager
import com.w57736e.yafeed.sync.WearableMessageManager
import com.w57736e.yafeed.sync.DataSyncListener
import com.google.android.gms.wearable.Wearable
import com.w57736e.yafeed.ui.components.WearDisconnectedOverlay
import com.w57736e.yafeed.ui.sources.AddEditSourceScreen
import com.w57736e.yafeed.ui.sources.SourcesScreen
import com.w57736e.yafeed.ui.sources.SourcesViewModel
import com.w57736e.yafeed.ui.settings.SettingsScreen
import com.w57736e.yafeed.ui.settings.SettingsViewModel
import com.w57736e.yafeed.ui.theme.YaFeedTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var dataSyncListener: DataSyncListener? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val preferenceManager = PreferenceManager(this)
        val wearableDataSyncManager = WearableDataSyncManager(this)
        val messageManager = WearableMessageManager(this)
        val sourcesViewModel = SourcesViewModel(RssSourceRepository(database.sourceDao(), wearableDataSyncManager))
        val settingsViewModel = SettingsViewModel(SettingsRepository(preferenceManager, wearableDataSyncManager))
        val connectionManager = WearableConnectionManager(this)

        // Register DataClient listener for sync
        dataSyncListener = DataSyncListener(this, lifecycleScope)
        Wearable.getDataClient(this).addListener(dataSyncListener!!)
        Log.d("MainActivity", "DataClient listener registered")

        lifecycleScope.launch {
            while (isActive) {
                connectionManager.checkConnection()
                delay(10000)
            }
        }

        // Bootstrap: clear local sources and request sync from Wear
        lifecycleScope.launch {
            delay(2000) // Wait for connection to establish
            
            Log.d("MainActivity", "Bootstrap: clearing local sources")
            database.sourceDao().deleteAllSources()
            
            Log.d("MainActivity", "Bootstrap: requesting sync from Wear")
            messageManager.requestSync()
            
            // Wait for sources to arrive, then mark bootstrapped
            delay(5000)
            sourcesViewModel.markBootstrapped()
            val sources = database.sourceDao().getAllSources().first()
            Log.d("MainActivity", "Bootstrap complete - sources: ${sources.size}")
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
    
    override fun onDestroy() {
        dataSyncListener?.let {
            Wearable.getDataClient(this).removeListener(it)
            Log.d("MainActivity", "DataClient listener unregistered")
        }
        super.onDestroy()
    }
}

sealed class Screen {
    object Sources : Screen()
    object Settings : Screen()
    data class AddEdit(val source: RssSource?) : Screen()
}
