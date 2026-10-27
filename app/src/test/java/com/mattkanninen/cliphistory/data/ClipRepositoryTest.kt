package com.mattkanninen.cliphistory.data

import com.mattkanninen.cliphistory.data.repository.ClipRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ClipRepositoryTest {

    private val clipboardDao: ClipboardDao = mockk(relaxed = true)
    private lateinit var repository: ClipRepositoryImpl

    @Before
    fun setup() {
        repository = ClipRepositoryImpl(clipboardDao)
    }

    @Test
    fun `saveClip inserts new clip when content is unique`() = runTest {
        val text = "https://kotlinlang.org"
        val hash = HashUtils.sha256(text)
        coEvery { clipboardDao.findByHash(hash) } returns null

        repository.saveClip(text)

        coVerify {
            clipboardDao.insert(match {
                it.content == text && it.contentHash == hash
            })
        }
    }

    @Test
    fun `saveClip updates timestamp when content already exists`() = runTest {
        val text = "Sample text"
        val hash = HashUtils.sha256(text)
        val existing = ClipboardEntry(id = 1, content = text, timestamp = 1000L, contentHash = hash)
        coEvery { clipboardDao.findByHash(hash) } returns existing

        repository.saveClip(text)

        coVerify {
            clipboardDao.updateTimestamp(1L, any())
        }
    }

    @Test
    fun `togglePin updates pin status in DAO`() = runTest {
        repository.togglePin(id = 42L, isPinned = false)

        coVerify {
            clipboardDao.togglePin(42L, true)
        }
    }
}
