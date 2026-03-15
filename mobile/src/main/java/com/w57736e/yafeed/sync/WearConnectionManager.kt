package com.w57736e.yafeed.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class WearConnectionManager(context: Context) {
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    private val maxRetries = 5

    suspend fun checkConnection() {
        var delay = 1000L

        repeat(maxRetries) { attempt ->
            try {
                val nodes = capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE)
                    .await()
                    .flatMap { it.value.nodes }

                if (nodes.isNotEmpty()) {
                    _isConnected.value = true
                    Log.d("WearConnection", "Connected to ${nodes.size} device(s)")
                    return
                }
            } catch (e: Exception) {
                Log.e("WearConnection", "Connection check failed (attempt ${attempt + 1}/$maxRetries)", e)
            }

            if (attempt < maxRetries - 1) {
                delay(delay)
                delay *= 2
            }
        }

        _isConnected.value = false
        Log.w("WearConnection", "Failed to connect after $maxRetries attempts")
    }
}
