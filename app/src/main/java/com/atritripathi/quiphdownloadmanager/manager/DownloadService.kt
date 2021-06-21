package com.atritripathi.quiphdownloadmanager.manager

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.atritripathi.quiphdownloadmanager.data.DownloadItem
import java.util.*

/**
 * Service for the activity to delegate the downloading process.
 */
class DownloadService : Service() {

    private val binder = LocalBinder()

    private val downloadManager = DownloadManager(this)

    fun download(
        url: String,
        onProgress: (DownloadItem) -> Unit = { }
    ): DownloadItem {
        return downloadManager.createDownloadTask(url, onProgress = onProgress)
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): DownloadService = this@DownloadService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}