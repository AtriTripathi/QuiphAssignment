package com.atritripathi.quiphdownloadmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atritripathi.quiphdownloadmanager.data.DownloadItem
import com.atritripathi.quiphdownloadmanager.data.DownloadRepository
import com.atritripathi.quiphdownloadmanager.data.DownloadStatus.CANCELLED
import com.atritripathi.quiphdownloadmanager.data.DownloadStatus.PAUSED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuiphViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {

    val downloadItems = repository.getDownloadItems()

    fun onPauseResumeToggle(downloadItem: DownloadItem) {
        downloadItem.progress?.status = PAUSED
        viewModelScope.launch {
            repository.saveDownloadItem(downloadItem)
        }
    }

    fun onCancelDownload(downloadItem: DownloadItem) {
        downloadItem.progress?.status = CANCELLED
        viewModelScope.launch {
            repository.saveDownloadItem(downloadItem)
        }
    }

    fun onDownload(downloadItem: DownloadItem) {
        viewModelScope.launch {
            repository.saveDownloadItem(downloadItem)
        }
    }
}
