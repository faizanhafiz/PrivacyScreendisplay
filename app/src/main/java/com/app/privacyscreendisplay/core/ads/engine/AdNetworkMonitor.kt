package com.app.privacyscreendisplay.core.ads.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thread-safe Network Availability Monitor for Ad retry management.
 * Pauses ad load retries when device is offline and automatically resumes them when connected.
 */
object AdNetworkMonitor {

    private const val TAG = "AdNetworkMonitor"

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private var onNetworkRestoredCallback: (() -> Unit)? = null
    private var isRegistered = false

    fun startMonitoring(context: Context, onNetworkRestored: () -> Unit = {}) {
        onNetworkRestoredCallback = onNetworkRestored
        if (isRegistered) return

        val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return

        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val initialConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _isNetworkAvailable.value = initialConnected

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val wasOffline = !_isNetworkAvailable.value
                    _isNetworkAvailable.value = true
                    AdLogger.i(TAG, "Network connection restored.")
                    if (wasOffline) {
                        onNetworkRestoredCallback?.invoke()
                    }
                }

                override fun onLost(network: Network) {
                    _isNetworkAvailable.value = false
                    AdLogger.w(TAG, "Network connection lost. Ad retries paused.")
                }
            })
            isRegistered = true
        } catch (e: Exception) {
            AdLogger.e(TAG, "Failed to register network callback", e)
        }
    }
}
