package com.mattkanninen.cliphistory.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_history")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val category: String = CATEGORY_TEXT,
    val sourceApp: String? = null
) {
    companion object {
        const val CATEGORY_TEXT = "Text"
        const val CATEGORY_URL = "URL"
        const val CATEGORY_CODE = "Code"
        const val CATEGORY_PINNED = "Pinned"
    }
}
