package com.example.medialibrary.music

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.music.ui.form.MusicFormViewModel
import models.music.Enums as MusicEnums
import models.music.MusicItem
import models.shared.Tag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import models.shared.Enums as SharedEnums

class MusicFormViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInitialMusicState() {
        val viewModel = MusicFormViewModel()
        val music = viewModel.music.value

        assertNotNull(music)
        assertTrue(music?.Artist.isNullOrEmpty())
        assertEquals(0, viewModel.items.value?.size)
    }

    @Test
    fun testUpdateFields() {
        val viewModel = MusicFormViewModel()

        viewModel.updateArtist("Pink Floyd")
        assertEquals("Pink Floyd", viewModel.music.value?.Artist)

        viewModel.updateCollectingPriority(SharedEnums.CollectingPriority.High)
        assertEquals(SharedEnums.CollectingPriority.High, viewModel.music.value?.CollectingPriority)
    }

    @Test
    fun testUpdateTag() {
        val viewModel = MusicFormViewModel()

        val existingTag = Tag().apply {
            Id = "12"
            Name = "Rock"
        }
        viewModel.updateTag(existingTag)
        assertEquals("12", viewModel.music.value?.Tag)

        viewModel.updateTagName("New Rock Tag")
        assertEquals("0", viewModel.music.value?.Tag)
    }

    @Test
    fun testAddUpdateAndDeleteMusicItems() {
        val viewModel = MusicFormViewModel()

        val item1 = MusicItem().apply {
            Title = "The Dark Side of the Moon"
            Year = 1973
            MusicGenre = MusicEnums.MusicGenre.Rock
        }
        val item2 = MusicItem().apply {
            Title = "The Wall"
            Year = 1979
            MusicGenre = MusicEnums.MusicGenre.Rock
        }

        viewModel.addOrUpdateItem(item1)
        assertEquals(1, viewModel.items.value?.size)

        viewModel.addOrUpdateItem(item2)
        assertEquals(2, viewModel.items.value?.size)

        val updatedItem1 = MusicItem().apply {
            Title = "Dark Side Remastered"
            Year = 1973
            MusicGenre = MusicEnums.MusicGenre.Rock
        }
        viewModel.addOrUpdateItem(updatedItem1, position = 0)
        assertEquals(2, viewModel.items.value?.size)
        assertEquals("Dark Side Remastered", viewModel.items.value?.get(0)?.Title)

        viewModel.deleteItem(0)
        assertEquals(1, viewModel.items.value?.size)
        assertEquals("The Wall", viewModel.items.value?.get(0)?.Title)
    }

    @Test
    fun testValidation() {
        val viewModel = MusicFormViewModel()

        val errors = viewModel.validate()
        assertTrue(errors.containsKey("artist")) // Artist is required

        viewModel.updateArtist("Pink Floyd")
        val updatedErrors = viewModel.validate()
        assertFalse(updatedErrors.containsKey("artist"))
    }

    @Test
    fun testGetSaveObject() {
        val viewModel = MusicFormViewModel()
        viewModel.updateArtist("Led Zeppelin")
        viewModel.updateTagName("Classic Rock")

        val musicItem = MusicItem().apply { Title = "Led Zeppelin IV"; Year = 1971 }
        viewModel.addOrUpdateItem(musicItem)

        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("Led Zeppelin", saveObj.Music?.Artist)
        assertEquals("Classic Rock", saveObj.NewTag)
        assertEquals(1, saveObj.Music?.MusicItems?.size)
    }
}
