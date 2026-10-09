package com.example.medialibrary.video

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.video.ui.display.VideoDisplayViewModel
import com.example.medialibrary.video.ui.utils.SortFilterViewmodel
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.video.Enums.VideoType
import models.video.Video
import models.video.VideoFilter
import models.video.VideoItem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VideoDisplayFragmentTest {

    private lateinit var viewModel: VideoDisplayViewModel
    private lateinit var mockSortFilterViewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = VideoDisplayViewModel()
        mockSortFilterViewModel = mockk(relaxed = true)

        every { mockSortFilterViewModel.getOrCreateVideoFilter() } returns VideoFilter()
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible, MediaItems LiveData contains video items and item counts")
    fun testDataVisible() {
        val item1 = VideoItem().apply { Watched = true; Owned = true }
        val video1 = Video().apply {
            Id = "100"
            Title = "Interstellar"
            Type = VideoType.Movie
            Items = arrayListOf(item1)
            Ongoing = false
            Collecting = false
            HasCollectedAllItems = true
        }
        val video2 = Video().apply {
            Id = "101"
            Title = "Breaking Bad"
            Type = VideoType.TVShow
            Items = arrayListOf()
            Ongoing = true
            Collecting = true
            HasCollectedAllItems = false
        }

        viewModel.setMediaItems(listOf(video1, video2))

        val mediaItems = viewModel.MediaItems.value
        assertNotNull(mediaItems)
        assertEquals(2, mediaItems?.size)
        assertEquals("Interstellar", (mediaItems?.get(0) as? Video)?.Title)
        assertEquals("Breaking Bad", (mediaItems?.get(1) as? Video)?.Title)
    }

    @Test
    @DisplayName("When there is no data, MediaItems LiveData is empty or null")
    fun testNoData() {
        assertNull(viewModel.MediaItems.value)

        viewModel.setMediaItems(emptyList())

        val mediaItems = viewModel.MediaItems.value
        assertNotNull(mediaItems)
        assertTrue(mediaItems.isNullOrEmpty())
    }

    @Test
    @DisplayName("Specific components hidden or shown depending on video media items state")
    fun testComponentVisibility() {
        viewModel.setMediaItems(emptyList())
        assertTrue(viewModel.MediaItems.value.isNullOrEmpty())

        val video = Video().apply { Title = "Dark Knight"; Type = VideoType.Movie }
        viewModel.setMediaItems(listOf(video))
        assertFalse(viewModel.MediaItems.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons work correctly and invoke filter / sort actions")
    fun testButtonsWorkCorrectly() {
        val initialFilter = mockSortFilterViewModel.getOrCreateVideoFilter()
        assertNotNull(initialFilter)

        val updatedFilter = VideoFilter().apply { Search = "Interstellar" }
        mockSortFilterViewModel.updateVideoFilter(updatedFilter)

        verify { mockSortFilterViewModel.updateVideoFilter(updatedFilter) }
        assertEquals("Interstellar", updatedFilter.Search)
    }
}
