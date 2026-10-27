package com.mattkanninen.cliphistory.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clipboard_entries",
    indices = [Index(value = ["contentHash"])]
)
data class ClipboardEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val contentHash: String,
    val sourcePackage: String? = null
)
