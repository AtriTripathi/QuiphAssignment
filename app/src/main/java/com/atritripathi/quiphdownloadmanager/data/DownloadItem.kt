package com.atritripathi.quiphdownloadmanager.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

@Entity(tableName = "download_items")
data class DownloadItem(
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    var fileName: String = "",
    var fileSize: Long = 0L,
    @Embedded
    var progress: DownloadProgress? = null
) {
    @Ignore
    val progressStateFlow: MutableStateFlow<DownloadProgress?> = MutableStateFlow(null)

    data class DownloadProgress(
        val bytesDownloaded: Long = 0,
        val totalBytes: Long = 0,
        var status: DownloadStatus = DownloadStatus.PENDING
    )
}