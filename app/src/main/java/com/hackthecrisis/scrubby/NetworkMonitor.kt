package com.hackthecrisis.scrubby

import android.content.Context
import android.content.SharedPreferences
import android.net.*
import android.util.Log

class NetworkMonitor(private val context: Context) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var sharedPreferences: SharedPreferences
    private val washNotification = WashNotification()

    companion object {
        const val TAG = "WashNotification"
    }

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network?) {
            Log.d(TAG,"networkcallback called from onLost")
            with(sharedPreferences.edit()) {
                putBoolean("isConnectedToWifi", false)
                commit()
            }
            //record wi-fi disconnect event
        }
        override fun onUnavailable() {
            Log.d(TAG,"networkcallback called from onUnvailable")
            with(sharedPreferences.edit()) {
                putBoolean("isConnectedToWifi", false)
                commit()
            }
        }

        override fun onLosing(network: Network?, maxMsToLive: Int) {
            Log.d(TAG,"networkcallback called from onLosing")
        }

        override fun onAvailable(network: Network?) {
            Log.d(TAG,"NetworkCallback network called from onAvailable ")
            val isConnectedToWifi = sharedPreferences.getBoolean("isConnectedToWifi", false)
            if (!isConnectedToWifi) {
                washNotification.createNotification(context)
            }
            with(sharedPreferences.edit()) {
                putBoolean("isConnectedToWifi", true)
                commit()
            }
        }
    }

    /**
     * Listens for changes in wifi connection by registering the networkCallback on
     * ConnectivityManager
     */
    fun listenForWifiChanges() {
        sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        //NetworkCallback that has been unregistered can be registered again
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.d(TAG,"NetworkCallback for Wi-fi was not registered or already unregistered")
        }

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    /**
     * Checks the current network status and stores the status in stored preferences if the status
     * has changed from not connected to wifi to connected it will trigger the wash notification
     */
    fun checkNetworkStatus() {
        val sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        val isMetered = connectivityManager.isActiveNetworkMetered
        if (isConnected && !isMetered) {
            val previousConnectionStatus = sharedPreferences.getBoolean("isConnectedToWifi", false)
            if (!previousConnectionStatus) {
                with(sharedPreferences.edit()) {
                    putBoolean("isConnectedToWifi", true)
                    commit()
                }
                washNotification.createNotification(context)
            }
        } else {
            with(sharedPreferences.edit()) {
                putBoolean("isConnectedToWifi", false)
                commit()
            }
        }
    }

}