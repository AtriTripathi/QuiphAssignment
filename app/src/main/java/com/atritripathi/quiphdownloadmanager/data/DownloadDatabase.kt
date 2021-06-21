package com.atritripathi.quiphdownloadmanager.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.atritripathi.quiphdownloadmanager.utils.Converters

@Database(
    entities = [DownloadItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DownloadDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "download_item_db"
    }

    abstract fun downloadItemDao(): DownloadItemDao
}