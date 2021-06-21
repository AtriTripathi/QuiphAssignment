package com.atritripathi.quiphdownloadmanager.utils.extensions

import com.atritripathi.quiphdownloadmanager.data.DownloadStatus
import com.atritripathi.quiphdownloadmanager.data.DownloadStatus.DOWNLOADING
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.Buffer
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import kotlin.coroutines.resumeWithException

/**
 * Extension function on [Call] object to handle download & save operation in background threads.
 * Also emits progress updates to the caller through [onProgress] callback.
 */
suspend fun Call.downloadAndSaveTo(
    outputFile: File,
    seekDistance: Long = 0,
    bufferSize: Long = DEFAULT_BUFFER_SIZE.toLong(),
    blockingDispatcher: CoroutineDispatcher = Dispatchers.IO,
    onProgress: ((downloaded: Long, total: Long, downloadStatus: DownloadStatus) -> Unit)? = null,
): File = withContext(blockingDispatcher) {
    suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    cont.resumeWithException(IOException("Unexpected HTTP code: ${response.code}"))
                    return
                }
                try {
                    val body = response.body
                    if (body == null) {
                        cont.resumeWithException(IllegalStateException("Body is null"))
                        return
                    }
                    val contentLength = body.contentLength()
                    val buffer = Buffer()
                    var finished = false
                    outputFile.sink().buffer().use { out ->
                        body.source()
                            .skip(seekDistance)    // seekDistance decides where to start or resume the download from.
                        body.source().use { source ->
                            var totalLength = 0L
                            // Keep reading from source as byteStreams in block sizes equal to
                            // buffer and write it into the sink until EOF is reached.
                            while (cont.isActive) {
                                val read = source.read(buffer, bufferSize)
                                if (read == -1L) {
                                    finished = true
                                    break
                                }
                                out.write(buffer, read)
                                out.flush()
                                totalLength += read
                                onProgress?.invoke(totalLength, contentLength, DOWNLOADING)
                            }
                        }
                    }
                    if (finished) {
                        cont.resume(outputFile) {}
                    } else {
                        cont.resumeWithException(IOException("Download cancelled"))
                    }
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
        })
    }
}

