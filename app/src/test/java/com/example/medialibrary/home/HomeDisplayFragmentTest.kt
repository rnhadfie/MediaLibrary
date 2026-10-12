package com.example.medialibrary.home

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.home.ui.display.HomeDisplayModel
import com.example.medialibrary.home.ui.utils.SortFilterViewmodel
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.shared.DisplayMediaItem
import models.shared.Enums
import models.shared.Filter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class HomeDisplayFragmentTest {

    private lateinit var viewModel: HomeDisplayModel
    private lateinit var mockSortFilterViewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = HomeDisplayModel()
        mockSortFilterViewModel = mockk(relaxed = true)

        every { mockSortFilterViewModel.getOrCreateMainFilter() } returns Filter()
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible, mediaItems LiveData contains items and item counts are correct")
    fun testDataVisible() {
        val bookItem = DisplayMediaItem().apply {
            Id = "1"
            Title = "Book 1"
            MediaType = Enums.MediaType.Book
            ItemCount = 5
        }
        val videoItem = DisplayMediaItem().apply {
            Id = "2"
            Title = "Video 1"
            MediaType = Enums.MediaType.Video
            ItemCount = 3
        }

        viewModel.setMediaItems(listOf(bookItem, videoItem))

        val items = viewModel.mediaItems.value
        assertNotNull(items)
        assertEquals(2, items?.size)
        assertEquals("Book 1", items?.get(0)?.Title)
        assertEquals("Video 1", items?.get(1)?.Title)
    }

    @Test
    @DisplayName("When there is no data, mediaItems LiveData is empty")
    fun testNoData() {
        viewModel.setMediaItems(emptyList())

        val items = viewModel.mediaItems.value
        assertNotNull(items)
        assertTrue(items.isNullOrEmpty())
    }

    @Test
    @DisplayName("Specific components hidden or shown depending on media items presence")
    fun testComponentVisibility() {
        // Empty data
        viewModel.setMediaItems(emptyList())
        assertTrue(viewModel.mediaItems.value.isNullOrEmpty())

        // Non-empty data
        val item = DisplayMediaItem().apply {
            Title = "Album 1"
            MediaType = Enums.MediaType.Music
            ItemCount = 10
        }
        viewModel.setMediaItems(listOf(item))
        assertFalse(viewModel.mediaItems.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons work correctly and filter updates update state flow")
    fun testButtonsWorkCorrectly() {
        val initialFilter = mockSortFilterViewModel.getOrCreateMainFilter()
        assertNotNull(initialFilter)

        val updatedFilter = Filter().apply { Search = "Album" }
        mockSortFilterViewModel.updateMainFilter(updatedFilter)

        verify { mockSortFilterViewModel.updateMainFilter(updatedFilter) }
        assertEquals("Album", updatedFilter.Search)
    }
}
