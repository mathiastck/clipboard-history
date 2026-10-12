package com.mattkanninen.cliphistory.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {

    @Query("SELECT * FROM clipboard_history ORDER BY isPinned DESC, timestamp DESC")
    fun getAllClips(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clipboard_history WHERE content LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun searchClips(query: String): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clipboard_history WHERE isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedClips(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clipboard_history WHERE content = :content LIMIT 1")
    suspend fun findByContent(content: String): ClipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: ClipEntity): Long

    @Query("UPDATE clipboard_history SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinStatus(id: Long, isPinned: Boolean)

    @Delete
    suspend fun deleteClip(clip: ClipEntity)

    @Query("DELETE FROM clipboard_history WHERE isPinned = 0")
    suspend fun clearUnpinnedHistory()

    @Query("DELETE FROM clipboard_history WHERE isPinned = 0 AND timestamp < :thresholdTimestamp")
    suspend fun deleteExpiredClips(thresholdTimestamp: Long)
}
