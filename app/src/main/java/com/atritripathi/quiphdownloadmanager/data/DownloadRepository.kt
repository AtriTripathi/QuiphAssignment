package com.atritripathi.quiphdownloadmanager.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DownloadRepository @Inject constructor(
    private val db: DownloadDatabase
) {
    private val downloadItemDao = db.downloadItemDao()

    fun getDownloadItems(): Flow<List<DownloadItem>> = downloadItemDao.getAllDownloadItems()

    suspend fun saveDownloadItem(downloadItem: DownloadItem) {
        downloadItemDao.insert(downloadItem)
    }
}