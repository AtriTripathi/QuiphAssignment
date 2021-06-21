package com.atritripathi.quiphdownloadmanager.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.atritripathi.quiphdownloadmanager.databinding.ActivityMainBinding
import com.atritripathi.quiphdownloadmanager.manager.DownloadService
import com.atritripathi.quiphdownloadmanager.utils.extensions.hideKeyboard
import com.atritripathi.quiphdownloadmanager.utils.getDownloadPercentage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var downloadService: DownloadService
    private var isServiceBound = false

    private lateinit var binding: ActivityMainBinding

    private val viewModel: QuiphViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as DownloadService.LocalBinder
            downloadService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    // Try downloading these links by passing them to `beginDownload()` and check the logs for streaming results.
    val imageUrl = "https://unsplash.com/photos/Yu2Bt4cjRxA/download?force=true"
    val videoUrl =
        "https://www.pexels.com/video/4706000/download/?search_query=&tracking_id=aw3934mxaif"
    val pdfUrl = "https://www.hq.nasa.gov/alsj/a17/A17_FlightPlan.pdf"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val downloadListAdapter = DownloadListAdapter(
            onPauseResumeClick = { downloadItem ->
                viewModel.onPauseResumeToggle(downloadItem)
            },
            onCancelDownloadClick = { downloadItem ->
                viewModel.onCancelDownload(downloadItem)
            }
        )

        initRecyclerView(downloadListAdapter)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnDownload.setOnClickListener {
                val enteredUrl = etUrl.text.toString()
                it.hideKeyboard()
                beginDownload(enteredUrl)
            }
        }
    }

    private fun initRecyclerView(downloadListAdapter: DownloadListAdapter) {
        binding.rvDownloadItems.apply {
            adapter = downloadListAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.downloadItems.collect { downloadItems ->
                downloadListAdapter.submitList(downloadItems)
            }
        }
    }

    private fun beginDownload(url: String) {
        downloadService.download(url, onProgress = { downloadItem ->
            /**
             * Please check the logs for streaming download progress data
             */
            println(
                "DOWNLOAD STAT -> File Name:${downloadItem.fileName} | " +
                        "Progress: ${getDownloadPercentage(downloadItem.progress)}"
            )
            viewModel.onDownload(downloadItem)
        })
    }

    override fun onStart() {
        super.onStart()
        Intent(this, DownloadService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isServiceBound = false
    }
}