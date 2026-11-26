package dev.wefter.bridge

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import org.json.JSONObject

/** Reports connectivity from `ConnectivityManager` and emits `network:changed` whenever it changes. */
class NetworkPlugin(context: Context, dispatcher: BridgeDispatcher) :
        WefterPlugin(context, dispatcher) {

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val debounceHandler = Handler(Looper.getMainLooper())
    /** Changes arrive in bursts when switching networks, so events are sent once things settle. */
    private val debounceDelayMs = 150L
    private val emitRunnable = Runnable {
        try {
            emit("network:changed", currentStatusJson())
        } catch (e: Exception) {
        }
    }

    init {
        registerNetworkCallback()
    }

    /** The current connection status. */
    @WefterMethod
    fun getStatus(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        try {
            resolve(callback, currentStatusJson())
        } catch (e: Exception) {
            reject(callback, "STATUS_FAILED", e.message ?: "Failed to get network status.")
        }
    }

    /** Listens for networks appearing, disappearing or changing capabilities. */
    private fun registerNetworkCallback() {
        try {
            val callback =
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            scheduleEmit()
                        }

                        override fun onLost(network: Network) {
                            scheduleEmit()
                        }

                        override fun onCapabilitiesChanged(
                                network: Network,
                                networkCapabilities: NetworkCapabilities
                        ) {
                            scheduleEmit()
                        }
                    }

            val request =
                    NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build()

            connectivityManager.registerNetworkCallback(request, callback)
            networkCallback = callback
        } catch (e: Exception) {
        }
    }

    /** Sends a `network:changed` event after the debounce delay, replacing any pending one. */
    private fun scheduleEmit() {
        debounceHandler.removeCallbacks(emitRunnable)
        debounceHandler.postDelayed(emitRunnable, debounceDelayMs)
    }

    /** Connection status of the active network: whether it can reach the internet and its transport. */
    private fun currentStatusJson(): JSONObject {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        if (activeNetwork == null || capabilities == null) {
            return JSONObject().put("connected", false).put("connectionType", "none")
        }

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) {
            return JSONObject().put("connected", false).put("connectionType", "none")
        }

        val connectionType =
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "bluetooth"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "vpn"
                    else -> "unknown"
                }

        return JSONObject().put("connected", true).put("connectionType", connectionType)
    }
}
