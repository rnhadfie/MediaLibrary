package com.example.medialibrary.video

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.video.ui.form.VideoFormViewModel
import controllers.VideoController
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import models.shared.Tag
import models.video.Enums as VideoEnums
import models.video.Enums.VideoType
import models.video.Video
import models.video.VideoItem
import models.video.VideoSetup
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VideoFormFragmentTest {

    private lateinit var viewModel: VideoFormViewModel
    private lateinit var mockController: VideoController

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = VideoFormViewModel()
        mockController = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible in edit mode, video and disc items load correctly")
    fun testDataVisibleInEditMode() {
        val videoItem1 = VideoItem().apply { Season = 1; DiscTitle = "Disc 1" }
        val existingVideo = Video().apply {
            Id = "10"
            Title = "Inception"
            Type = VideoType.Movie
            VideoTag = VideoEnums.VideoTag.LiveAction
            Collecting = true
            Ongoing = false
            Items = arrayListOf(videoItem1)
        }

        val setup = VideoSetup().apply {
            Tag = arrayListOf(Tag().apply { Id = "12"; Name = "Sci-Fi" })
        }

        every { mockController.GetVideo("10") } returns existingVideo

        viewModel.loadVideo("10", mockController, setup)

        val loadedVideo = viewModel.video.value
        assertNotNull(loadedVideo)
        assertEquals("Inception", loadedVideo?.Title)
        assertEquals(VideoType.Movie, loadedVideo?.Type)
        assertTrue(loadedVideo?.Collecting == true)

        val loadedItems = viewModel.items.value
        assertNotNull(loadedItems)
        assertEquals(1, loadedItems?.size)
        assertEquals("Disc 1", loadedItems?.get(0)?.DiscTitle)
    }

    @Test
    @DisplayName("When there is no data in new mode, initial state is empty and validation catches missing title")
    fun testNoDataNewMode() {
        val initial = viewModel.video.value
        assertNotNull(initial)
        assertTrue(initial?.Title.isNullOrEmpty())
        assertEquals(VideoType.NoneSelected, initial?.Type)
        assertEquals(0, viewModel.items.value?.size)

        // Validation fails when title is missing
        val errors = viewModel.validate()
        assertTrue(errors.containsKey("title"))

        // Validation passes when title is updated
        viewModel.updateTitle("Interstellar")
        viewModel.updateYear(2014)
        viewModel.updateRating(VideoEnums.Rating.PG13)
        assertEquals(2014, viewModel.video.value?.Year)
        assertEquals(VideoEnums.Rating.PG13, viewModel.video.value?.Rating)
        val updatedErrors = viewModel.validate()
        assertFalse(updatedErrors.containsKey("title"))
    }

    @Test
    @DisplayName("Specific components hidden or shown depending on video type and tag")
    fun testComponentVisibility() {
        viewModel.updateVideoType(VideoType.Movie)
        assertEquals(VideoType.Movie, viewModel.video.value?.Type)

        viewModel.updateVideoTag(VideoEnums.VideoTag.Anime)
        assertEquals(VideoEnums.VideoTag.Anime, viewModel.video.value?.VideoTag)
    }

    @Test
    @DisplayName("Buttons and form actions work correctly")
    fun testButtonsWorkCorrectly() {
        viewModel.updateTitle("The Dark Knight")
        viewModel.updateTagName("DC Comics")
        viewModel.toggleCollecting(true)

        val item = VideoItem().apply { Season = 1; DiscTitle = "Special Features" }
        viewModel.addOrUpdateItem(item)

        assertEquals(1, viewModel.items.value?.size)

        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("The Dark Knight", saveObj.video?.Title)
        assertEquals("DC Comics", saveObj.NewTag)
    }
}
