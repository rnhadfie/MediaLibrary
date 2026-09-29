package com.example.medialibrary.music

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.backend.models.music.Enums
import com.example.medialibrary.backend.models.shared.GenreObject
import com.example.medialibrary.backend.models.shared.Tag
import com.example.medialibrary.music.ui.form.MusicFormViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MusicFormViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInitialMusicState() {
        val viewModel = MusicFormViewModel()
        val music = viewModel.music.value

        assertNotNull(music)
        assertEquals(null, music?.MusicGenre)
        assertNotNull(music?.Genre)
    }

    @Test
    fun testUpdateFields() {
        val viewModel = MusicFormViewModel()

        viewModel.updateTitle("Dune")
        assertEquals("Dune", viewModel.music.value?.Title)


        viewModel.updateArtist("Artist X")
        assertEquals("Artist X", viewModel.music.value?.Artist)

        viewModel.updateMusicGenre(Enums.MusicGenre.Rock)
        assertEquals(Enums.MusicGenre.Rock, viewModel.music.value?.MusicGenre)
    }

    @Test
    fun testUpdatePublisherAndTag() {
        val viewModel = MusicFormViewModel()

        val existingTag = Tag().apply {
            Id = "12"
            Name = "Sci-Fi"
        }
        viewModel.updateTag(existingTag)
        assertEquals("12", viewModel.music.value?.Tag)

        viewModel.updateTagName("New SciFi Tag")
        assertEquals("0", viewModel.music.value?.Tag)
    }

    @Test
    fun testToggleTogglesAndGenres() {
        val viewModel = MusicFormViewModel()

        viewModel.toggleCollecting(true)
        assertTrue(viewModel.music.value?.Collecting == true)


        viewModel.toggleCollectionComplete(true)
        assertTrue(viewModel.music.value?.HasCollectedAllItems == true)

    }



    @Test
    fun testValidation() {
        val viewModel = MusicFormViewModel()

        assertNotNull(viewModel.validate()) // Title is required

        viewModel.updateTitle("The Hobbit")
        assertEquals(0, viewModel.validate().size)
    }

    @Test
    fun testGetSaveObject() {
        val viewModel = MusicFormViewModel()
        viewModel.updateTitle("Foundation")
        viewModel.updateTagName("Classic")

        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("Foundation", saveObj.Music?.Title)
        assertEquals("Classic", saveObj.NewTag)
    }
}
