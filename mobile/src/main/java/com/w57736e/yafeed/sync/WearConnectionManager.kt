package com.w57736e.yafeed.sync

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class WearConnectionManager(context: Context) {
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    suspend fun checkConnection() {
        try {
            val nodes = capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE)
                .await()
                .flatMap { it.value.nodes }
            _isConnected.value = nodes.isNotEmpty()
        } catch (e: Exception) {
            _isConnected.value = false
        }
    }
}
