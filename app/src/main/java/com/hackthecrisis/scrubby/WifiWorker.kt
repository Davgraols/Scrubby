package com.hackthecrisis.scrubby

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * The worker class that will trigger doWork periodically by WorkManager
 */
class WifiWorker(private val context: Context, params: WorkerParameters): Worker(context, params) {

    companion object {
        const val TAG = "WifiWorker"
    }

    override fun doWork(): Result {
        Log.d(TAG, "Doing periodic work")
        val monitor = NetworkMonitor(context)
        monitor.checkNetworkStatus()
        monitor.listenForWifiChanges()
        return Result.success()
    }
}