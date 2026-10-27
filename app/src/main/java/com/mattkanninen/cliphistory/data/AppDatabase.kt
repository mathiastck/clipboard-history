package com.mattkanninen.cliphistory.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ClipboardEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao

    companion object {
        const val DATABASE_NAME = "clipboard_database.db"
    }
}
