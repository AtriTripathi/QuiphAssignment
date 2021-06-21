package com.atritripathi.quiphdownloadmanager.manager

import android.content.Context
import com.atritripathi.quiphdownloadmanager.data.DownloadItem
import com.atritripathi.quiphdownloadmanager.data.DownloadItem.DownloadProgress
import com.atritripathi.quiphdownloadmanager.data.DownloadStatus.*
import com.atritripathi.quiphdownloadmanager.utils.RetryInterceptor
import com.atritripathi.quiphdownloadmanager.utils.extensions.downloadAndSaveTo
import com.atritripathi.quiphdownloadmanager.utils.getRandomFileName
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * The [maxRetry] parameter allows us to set the amount of retry we want by passing it to
 * the custom [RetryInterceptor] class.
 *
 * The Manager keeps an in-memory database using a map and stores the download item IDs as
 * keys associated with Pair of download job and its corresponding flow.
 */
class DownloadManager(
    private val context: Context,
    private val maxRetry: Int = 3,
    private val downloadScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    private val downloadMap: MutableMap<String, Pair<Job, MutableStateFlow<DownloadProgress?>>> =
        mutableMapOf()

    // Create network client and add the retry interceptor
    private val networkClient: OkHttpClient by lazy {
        OkHttpClient().newBuilder()
            .addInterceptor(RetryInterceptor(maxRetry))
            .build()
    }

    // Creates a new download task
    fun createDownloadTask(
        url: String,
        onProgress: (DownloadItem) -> Unit = { },
        onError: (t: Throwable) -> Unit = { },
        onCompletion: (isCompleted: Boolean) -> Unit = { }
    ): DownloadItem {

        return startDownload(
            downloadItem = DownloadItem(url = url),
            onProgress = onProgress,
            onError = onError,
            onCompletion = onCompletion
        )
    }

    private fun startDownload(
        downloadItem: DownloadItem,
        seekDistance: Long = 0,     // SeekDistance decides where the download should resume from.
        onProgress: (DownloadItem) -> Unit,
        onError: (t: Throwable) -> Unit,
        onCompletion: (isCompleted: Boolean) -> Unit
    ): DownloadItem {
        val request = Request.Builder().get()
            .url(downloadItem.url)
            .build()

        // Get the already present file or create a new file with random name
        var file = File(context.getExternalFilesDir(null), getRandomFileName(10))
        if (file.exists() && file.isFile) {
            file = File(context.getExternalFilesDir(null), downloadItem.fileName)
        }
        downloadItem.fileName = file.name

        try {
            val downloadJob = downloadScope.launch {
                file = networkClient.newCall(request).downloadAndSaveTo(file, seekDistance,
                    onProgress = { downloadedBytes, totalBytes, downloadStatus ->
                        val currentDownloadProgress =
                            DownloadProgress(downloadedBytes, totalBytes, downloadStatus)

                        // Emit the progress values into the StateFlow of the in-memory map.
                        downloadMap[downloadItem.id]?.second?.value = currentDownloadProgress
                        downloadItem.progressStateFlow.value = currentDownloadProgress
                        downloadItem.progress = currentDownloadProgress

                        // Also emit the progress data like a stream up the `onProgress()` callback.
                        onProgress.invoke(downloadItem)
                    }
                )
            }

            // Update the download progress status
            if (file.exists() && file.isFile && downloadJob.isCompleted
                && downloadItem.progress?.bytesDownloaded == downloadItem.progress?.totalBytes
            ) {
                downloadItem.progress = downloadItem.progress?.copy(status = COMPLETED)
                downloadMap[downloadItem.id] = Pair(downloadJob, downloadItem.progressStateFlow)
                onCompletion.invoke(true)
            }

        } catch (e: Exception) {
            onError.invoke(e)
            e.printStackTrace()
        } finally {
            return downloadItem.copy(
                fileName = file.name,
                fileSize = file.totalSpace,
                progress = downloadItem.progressStateFlow.value,
            )
        }
    }

    // Same as `createDownloadTask()` but with the difference that the `seekDistance` is from pre-existing file
    fun resumeDownload(
        downloadItem: DownloadItem,
        onProgress: (DownloadItem) -> Unit = { },
        onError: (t: Throwable) -> Unit = { },
        onCompletion: (isCompleted: Boolean) -> Unit = { }
    ) {
        // Add a new Download Job to the in-memory map.
        downloadMap[downloadItem.url] = Pair(Job(), downloadItem.progressStateFlow)

        // Resume downloading from the seek distance of paused download item.
        startDownload(
            downloadItem = downloadItem,
            seekDistance = downloadItem.progress?.bytesDownloaded ?: 0,
            onProgress = onProgress,
            onError = onError,
            onCompletion = onCompletion
        )
    }


    // Pause the download from its id by canceling its Job in the in-memory map.
    // This action saves the downloaded data and closed the IO and network stream correctly.
    fun pauseDownload(downloadItem: DownloadItem): Boolean {
        val job = downloadMap[downloadItem.id]?.first
        val isJobCancelled = job?.isCancelled ?: false
        return if (!isJobCancelled) {
            job?.cancel()
            downloadItem.progress = downloadItem.progress?.copy(status = PAUSED)
            true
        } else false
    }

    // This cancel the download by cancelling the job and removing its entry from
    // the in-memory map. This also deletes the file incomplete file from the Storage.
    fun cancelDownload(downloadItem: DownloadItem): Boolean {
        val job = downloadMap[downloadItem.id]?.first
        val isJobCancelled = job?.isCancelled ?: false
        return if (!isJobCancelled) {
            job?.cancel()
            downloadItem.progress = downloadItem.progress?.copy(status = CANCELLED)
            File(context.getExternalFilesDir(null), downloadItem.fileName).delete()
            downloadMap.remove(downloadItem.id)
            true
        } else false
    }
}
