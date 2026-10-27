package com.mattkanninen.cliphistory.data.repository

import com.mattkanninen.cliphistory.data.ClipboardDao
import com.mattkanninen.cliphistory.data.ClipboardEntry
import com.mattkanninen.cliphistory.data.HashUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipRepositoryImpl @Inject constructor(
    private val clipboardDao: ClipboardDao
) : ClipRepository {

    override fun observeAll(): Flow<List<ClipboardEntry>> = clipboardDao.observeAll()

    override fun search(query: String): Flow<List<ClipboardEntry>> = clipboardDao.search(query)

    override suspend fun saveClip(content: String, sourcePackage: String?) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        val hash = HashUtils.sha256(trimmed)
        val existing = clipboardDao.findByHash(hash)
        val currentTime = System.currentTimeMillis()

        if (existing != null) {
            clipboardDao.updateTimestamp(existing.id, currentTime)
        } else {
            val entry = ClipboardEntry(
                content = trimmed,
                timestamp = currentTime,
                isPinned = false,
                contentHash = hash,
                sourcePackage = sourcePackage
            )
            clipboardDao.insert(entry)
        }
    }

    override suspend fun togglePin(id: Long, isPinned: Boolean) {
        clipboardDao.togglePin(id, !isPinned)
    }

    override suspend fun deleteById(id: Long) {
        clipboardDao.deleteById(id)
    }

    override suspend fun clearUnpinned() {
        clipboardDao.clearUnpinned()
    }
}
