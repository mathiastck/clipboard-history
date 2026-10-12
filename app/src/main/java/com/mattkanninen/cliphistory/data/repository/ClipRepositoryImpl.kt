package com.mattkanninen.cliphistory.data.repository

import com.mattkanninen.cliphistory.data.local.ClipDao
import com.mattkanninen.cliphistory.data.local.ClipEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipRepositoryImpl @Inject constructor(
    private val clipDao: ClipDao
) : ClipRepository {

    override fun getAllClips(): Flow<List<ClipEntity>> = clipDao.getAllClips()

    override fun searchClips(query: String): Flow<List<ClipEntity>> = clipDao.searchClips(query)

    override fun getPinnedClips(): Flow<List<ClipEntity>> = clipDao.getPinnedClips()

    override suspend fun saveClip(content: String, sourceApp: String?) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        val existing = clipDao.findByContent(trimmed)
        if (existing != null) {
            // Update timestamp of existing clip
            clipDao.insertClip(existing.copy(timestamp = System.currentTimeMillis()))
        } else {
            val category = determineCategory(trimmed)
            val entity = ClipEntity(
                content = trimmed,
                category = category,
                sourceApp = sourceApp
            )
            clipDao.insertClip(entity)
        }
    }

    override suspend fun togglePin(clipId: Long, currentPinnedState: Boolean) {
        clipDao.updatePinStatus(clipId, !currentPinnedState)
    }

    override suspend fun deleteClip(clip: ClipEntity) {
        clipDao.deleteClip(clip)
    }

    override suspend fun clearUnpinnedHistory() {
        clipDao.clearUnpinnedHistory()
    }

    private fun determineCategory(text: String): String {
        return when {
            text.startsWith("http://") || text.startsWith("https://") -> ClipEntity.CATEGORY_URL
            text.contains("{") && text.contains("}") || text.contains("val ") || text.contains("fun ") -> ClipEntity.CATEGORY_CODE
            else -> ClipEntity.CATEGORY_TEXT
        }
    }
}
