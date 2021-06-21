package com.atritripathi.quiphdownloadmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadItemDao {

    @Query("SELECT * FROM download_items")
    fun getAllDownloadItems(): Flow<List<DownloadItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(downloadItem: DownloadItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(downloadItems: List<DownloadItem>)
}