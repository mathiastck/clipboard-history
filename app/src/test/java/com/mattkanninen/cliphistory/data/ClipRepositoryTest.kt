package com.mattkanninen.cliphistory.data

import com.mattkanninen.cliphistory.data.local.ClipDao
import com.mattkanninen.cliphistory.data.local.ClipEntity
import com.mattkanninen.cliphistory.data.repository.ClipRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ClipRepositoryTest {

    private val clipDao: ClipDao = mockk(relaxed = true)
    private lateinit var repository: ClipRepositoryImpl

    @Before
    fun setup() {
        repository = ClipRepositoryImpl(clipDao)
    }

    @Test
    fun `saveClip inserts new clip when content is unique`() = runTest {
        val text = "https://kotlinlang.org"
        coEvery { clipDao.findByContent(text) } returns null

        repository.saveClip(text)

        coVerify {
            clipDao.insertClip(match {
                it.content == text && it.category == ClipEntity.CATEGORY_URL
            })
        }
    }

    @Test
    fun `saveClip updates timestamp when content already exists`() = runTest {
        val text = "Sample text"
        val existing = ClipEntity(id = 1, content = text, timestamp = 1000L)
        coEvery { clipDao.findByContent(text) } returns existing

        repository.saveClip(text)

        coVerify {
            clipDao.insertClip(match {
                it.id == 1L && it.content == text && it.timestamp > 1000L
            })
        }
    }

    @Test
    fun `togglePin updates pin status in DAO`() = runTest {
        repository.togglePin(clipId = 42L, currentPinnedState = false)

        coVerify {
            clipDao.updatePinStatus(42L, true)
        }
    }
}
