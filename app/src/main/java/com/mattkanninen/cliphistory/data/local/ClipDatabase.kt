package com.mattkanninen.cliphistory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ClipEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ClipDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao

    companion object {
        const val DATABASE_NAME = "pixel_clip_history.db"
    }
}
