package com.mattkanninen.cliphistory.data.repository

import com.mattkanninen.cliphistory.data.ClipboardEntry
import kotlinx.coroutines.flow.Flow

interface ClipRepository {
    fun observeAll(): Flow<List<ClipboardEntry>>
    fun search(query: String): Flow<List<ClipboardEntry>>
    suspend fun saveClip(content: String, sourcePackage: String? = null)
    suspend fun togglePin(id: Long, isPinned: Boolean)
    suspend fun deleteById(id: Long)
    suspend fun clearUnpinned()
}
