package com.mattkanninen.cliphistory.data.repository

import com.mattkanninen.cliphistory.data.local.ClipEntity
import kotlinx.coroutines.flow.Flow

interface ClipRepository {
    fun getAllClips(): Flow<List<ClipEntity>>
    fun searchClips(query: String): Flow<List<ClipEntity>>
    fun getPinnedClips(): Flow<List<ClipEntity>>
    suspend fun saveClip(content: String, sourceApp: String? = null)
    suspend fun togglePin(clipId: Long, currentPinnedState: Boolean)
    suspend fun deleteClip(clip: ClipEntity)
    suspend fun clearUnpinnedHistory()
}
