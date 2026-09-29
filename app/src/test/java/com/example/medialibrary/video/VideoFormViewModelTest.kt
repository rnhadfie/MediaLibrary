package com.example.medialibrary.video

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.backend.models.shared.GenreObject
import com.example.medialibrary.backend.models.shared.Tag
import com.example.medialibrary.backend.models.video.Enums
import com.example.medialibrary.backend.models.video.VideoItem

import com.example.medialibrary.video.ui.form.VideoFormViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class VideoFormViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInitialVideoState() {
        val viewModel = VideoFormViewModel()
        val video = viewModel.video.value

        assertNotNull(video)
        assertEquals(Enums.VideoType.NoneSelected, video?.Type)
        assertEquals(null, video?.VideoTag)
        assertNotNull(video?.Genre)
        assertEquals(0, viewModel.items.value?.size)
    }

    @Test
    fun testUpdateFields() {
        val viewModel = VideoFormViewModel()

        viewModel.updateTitle("Dune")
        assertEquals("Dune", viewModel.video.value?.Title)

        viewModel.updateVideoTag(Enums.VideoTag.Anime)
        assertEquals(Enums.VideoTag.Anime, viewModel.video.value?.VideoTag)

        viewModel.updateVideoType(Enums.VideoType.Movie)
        assertEquals(Enums.VideoType.Movie, viewModel.video.value?.Type)
    }

    @Test
    fun testUpdateTag() {
        val viewModel = VideoFormViewModel()

        val existingTag = Tag().apply {
            Id = "12"
            Name = "Sci-Fi"
        }
        viewModel.updateTag(existingTag)
        assertEquals("12", viewModel.video.value?.Tag)

        viewModel.updateTagName("New SciFi Tag")
        assertEquals("0", viewModel.video.value?.Tag)
    }

    @Test
    fun testToggleTogglesAndGenres() {
        val viewModel = VideoFormViewModel()

        viewModel.toggleCollecting(true)
        assertTrue(viewModel.video.value?.Collecting == true)

        viewModel.toggleCompleted(true)
        assertTrue(viewModel.video.value?.Ongoing == true)

        viewModel.toggleCollectionComplete(true)
        assertTrue(viewModel.video.value?.HasCollectedAllItems == true)

        val genreObj = GenreObject().apply {
            genreId = 1
            genreName = "Fantasy"
        }
        viewModel.toggleGenre(genreObj)
        assertTrue(viewModel.selectedGenres.value?.contains(genreObj) == true)
        assertTrue(viewModel.video.value?.Genre?.contains(1) == true)

        viewModel.toggleGenre(genreObj)
        assertFalse(viewModel.selectedGenres.value?.contains(genreObj) == true)
        assertFalse(viewModel.video.value?.Genre?.contains(1) == true)
    }

    @Test
    fun testAddUpdateAndDeleteItems() {
        val viewModel = VideoFormViewModel()

        val item1 = VideoItem().apply {
            Season = 1
            DiscTitle = "Volume 1"
        }
        val item2 = VideoItem().apply {
            Season = 2
            DiscTitle = "Volume 2"
        }

        viewModel.addOrUpdateItem(item1)
        assertEquals(1, viewModel.items.value?.size)

        viewModel.addOrUpdateItem(item2)
        assertEquals(2, viewModel.items.value?.size)

        val updatedItem1 = VideoItem().apply {
            Season = 1
            DiscTitle = "Volume 1 Updated"
        }
        viewModel.addOrUpdateItem(updatedItem1, position = 0)
        assertEquals(2, viewModel.items.value?.size)
        assertEquals("Volume 1 Updated", viewModel.items.value?.get(0)?.DiscTitle)

        viewModel.deleteItem(0)
        assertEquals(1, viewModel.items.value?.size)
        assertEquals("Volume 2", viewModel.items.value?.get(0)?.DiscTitle)
    }

    @Test
    fun testValidation() {
        val viewModel = VideoFormViewModel()

        assertNotNull(viewModel.validate()) // Title is required

        viewModel.updateTitle("The Hobbit")
        assertEquals(0, viewModel.validate().size)
    }

    @Test
    fun testGetSaveObject() {
        val viewModel = VideoFormViewModel()
        viewModel.updateTitle("Foundation")
        viewModel.updateTagName("Classic")

        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("Foundation", saveObj.video?.Title)
        assertEquals("Classic", saveObj.NewTag)
    }
}
