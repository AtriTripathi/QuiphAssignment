package com.atritripathi.quiphdownloadmanager.data

sealed class DownloadStatus {
    abstract val value: Int

    object PENDING : DownloadStatus() {
        override val value: Int = 1
        override fun toString() = "Pending"
    }

    object DOWNLOADING : DownloadStatus() {
        override val value: Int = 2
        override fun toString() = "Downloading"
    }

    object PAUSED : DownloadStatus() {
        override val value: Int = 3
        override fun toString() = "Paused"
    }

    object COMPLETED : DownloadStatus() {
        override val value: Int = 4
        override fun toString() = "Completed"
    }

    object CANCELLED : DownloadStatus() {
        override val value: Int = 5
        override fun toString() = "Cancelled"
    }
}