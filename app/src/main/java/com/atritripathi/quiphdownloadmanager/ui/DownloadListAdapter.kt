package com.atritripathi.quiphdownloadmanager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atritripathi.quiphdownloadmanager.data.DownloadItem
import com.atritripathi.quiphdownloadmanager.data.DownloadStatus.*
import com.atritripathi.quiphdownloadmanager.databinding.ItemDownloadBinding
import com.atritripathi.quiphdownloadmanager.ui.DownloadListAdapter.DownloadItemViewHolder
import com.atritripathi.quiphdownloadmanager.utils.getDownloadPercentage
import com.atritripathi.quiphdownloadmanager.utils.getDownloadProgress

class DownloadListAdapter(
    private val onPauseResumeClick: (DownloadItem) -> Unit,
    private val onCancelDownloadClick: (DownloadItem) -> Unit
) : ListAdapter<DownloadItem, DownloadItemViewHolder>(DownloadItemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        val binding =
            ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DownloadItemViewHolder(binding,
            onPauseResumeClick = { position ->
                val downloadItem = getItem(position)
                if (downloadItem != null) {
                    onPauseResumeClick(downloadItem)
                }
            },
            onCancelDownloadClick = { position ->
                val downloadItem = getItem(position)
                if (downloadItem != null) {
                    onCancelDownloadClick(downloadItem)
                }
            })
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    class DownloadItemViewHolder(
        private val binding: ItemDownloadBinding,
        private val onPauseResumeClick: (Int) -> Unit,
        private val onCancelDownloadClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(downloadItem: DownloadItem) {
            binding.apply {
                tvFileName.text = downloadItem.fileName
                tvFileUrl.text = "URL: ${downloadItem.url}"
                tvDownloadProgress.text = getDownloadProgress(downloadItem.progress)
                tvPercentage.text = getDownloadPercentage(downloadItem.progress)
                tvDownloadStatus.text = downloadItem.progress?.status.toString()
                btnPauseResume.text = when (downloadItem.progress?.status) {
                    PAUSED -> "Resume"
                    else -> "Pause"
                }
                btnPauseResume.isEnabled = downloadItem.progress?.status != PENDING
                        && downloadItem.progress?.status != COMPLETED
                        && downloadItem.progress?.status != CANCELLED
                btnCancelDownload.isEnabled = downloadItem.progress?.status != COMPLETED
                        && downloadItem.progress?.status != CANCELLED
            }
        }

        init {
            binding.apply {
                btnPauseResume.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onPauseResumeClick(position)
                    }
                }
                btnCancelDownload.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onCancelDownloadClick(position)
                    }
                }
            }
        }
    }
}

object DownloadItemComparator : DiffUtil.ItemCallback<DownloadItem>() {
    override fun areItemsTheSame(oldItem: DownloadItem, newItem: DownloadItem) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: DownloadItem, newItem: DownloadItem) =
        oldItem == newItem
}