package com.example.medialibrary.music

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.music.ui.form.MusicFormViewModel
import controllers.MusicController
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import models.music.Enums as MusicEnums
import models.music.Music
import models.music.MusicItem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MusicFormFragmentTest {

    private lateinit var viewModel: MusicFormViewModel
    private lateinit var mockController: MusicController

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = MusicFormViewModel()
        mockController = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible in edit mode, artist and music items load correctly")
    fun testDataVisibleInEditMode() {
        val musicItem1 = MusicItem().apply { Title = "Abbey Road"; Year = 1969; MusicGenre = MusicEnums.MusicGenre.Rock }
        val existingMusic = Music().apply {
            Id = "10"
            Artist = "The Beatles"
            Tag = "12"
            MusicItems = arrayListOf(musicItem1)
        }

        every { mockController.GetMusic("10") } returns existingMusic

        viewModel.loadCd("10", mockController)

        val loadedMusic = viewModel.music.value
        assertNotNull(loadedMusic)
        assertEquals("The Beatles", loadedMusic?.Artist)
        assertEquals("12", loadedMusic?.Tag)

        val items = viewModel.items.value
        assertNotNull(items)
        assertEquals(1, items?.size)
        assertEquals("Abbey Road", items?.get(0)?.Title)
    }

    @Test
    @DisplayName("When there is no data in new mode, initial form state has default values and validation catches missing artist")
    fun testNoDataNewMode() {
        val initial = viewModel.music.value
        assertNotNull(initial)
        assertTrue(initial?.Artist.isNullOrEmpty())

        // Validation fails when artist is empty
        val validationErrors = viewModel.validate()
        assertTrue(validationErrors.containsKey("artist"))

        // Validation passes when artist is filled
        viewModel.updateArtist("Pink Floyd")
        val updatedErrors = viewModel.validate()
        assertFalse(updatedErrors.containsKey("artist"))
    }

    @Test
    @DisplayName("Buttons and form actions work correctly")
    fun testButtonsWorkCorrectly() {
        viewModel.updateArtist("Led Zeppelin")
        viewModel.updateTagName("Classic Rock")

        val musicItem = MusicItem().apply { Title = "IV"; Year = 1971; MusicGenre = MusicEnums.MusicGenre.Rock }
        viewModel.addOrUpdateItem(musicItem)

        assertEquals(1, viewModel.items.value?.size)

        // Test updating item at position 0
        val updatedItem = MusicItem().apply { Title = "IV (Remastered)"; Year = 1971; MusicGenre = MusicEnums.MusicGenre.Rock; Collecting = true; Cover = byteArrayOf(1, 2, 3) }
        viewModel.addOrUpdateItem(updatedItem, 0)
        assertEquals(1, viewModel.items.value?.size)
        assertEquals("IV (Remastered)", viewModel.items.value?.get(0)?.Title)
        assertTrue(viewModel.items.value?.get(0)?.Collecting == true)

        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("Led Zeppelin", saveObj.Music?.Artist)
        assertEquals("Classic Rock", saveObj.NewTag)
    }
}
