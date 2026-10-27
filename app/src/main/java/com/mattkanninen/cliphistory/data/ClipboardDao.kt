package com.mattkanninen.cliphistory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Query("SELECT * FROM clipboard_entries ORDER BY isPinned DESC, timestamp DESC")
    fun observeAll(): Flow<List<ClipboardEntry>>

    @Query("SELECT * FROM clipboard_entries WHERE content LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun search(query: String): Flow<List<ClipboardEntry>>

    @Query("SELECT * FROM clipboard_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): ClipboardEntry?

    @Query("SELECT * FROM clipboard_entries WHERE contentHash = :hash LIMIT 1")
    suspend fun findByHash(hash: String): ClipboardEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ClipboardEntry): Long

    @Query("UPDATE clipboard_entries SET timestamp = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(id: Long, timestamp: Long)

    @Query("UPDATE clipboard_entries SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(id: Long, isPinned: Boolean)

    @Query("DELETE FROM clipboard_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM clipboard_entries WHERE isPinned = 0")
    suspend fun clearUnpinned()
}
