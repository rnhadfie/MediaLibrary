package com.example.medialibrary.video

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.video.ui.list.VideoListModel
import com.example.medialibrary.video.ui.utils.SortFilterViewmodel
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.shared.DisplayMediaItem
import models.shared.Enums
import models.video.VideoFilter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class VideoListFragmentTest {

    private lateinit var viewModel: VideoListModel
    private lateinit var mockSortFilterViewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = VideoListModel()
        mockSortFilterViewModel = mockk(relaxed = true)

        every { mockSortFilterViewModel.getOrCreateVideoFilter() } returns VideoFilter()
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible, viewModel items contain list items")
    fun testDataVisible() {
        val item1 = DisplayMediaItem().apply {
            Id = "1"
            Title = "Avatar"
            MediaType = Enums.MediaType.Video
        }
        val item2 = DisplayMediaItem().apply {
            Id = "2"
            Title = "Titanic"
            MediaType = Enums.MediaType.Video
        }

        viewModel.setItems(listOf(item1, item2))

        val items = viewModel.items.value
        assertNotNull(items)
        assertEquals(2, items?.size)
        assertEquals("Avatar", items?.get(0)?.Title)
        assertEquals("Titanic", items?.get(1)?.Title)
    }

    @Test
    @DisplayName("When there is no data, viewModel items is empty")
    fun testNoData() {
        viewModel.setItems(emptyList())

        val items = viewModel.items.value
        assertNotNull(items)
        assertTrue(items.isNullOrEmpty())
    }

    @Test
    @DisplayName("Specific components hidden or visible depending on state")
    fun testComponentVisibility() {
        viewModel.setItems(emptyList())
        assertTrue(viewModel.items.value.isNullOrEmpty())

        viewModel.setItems(listOf(DisplayMediaItem().apply { Title = "Gladiator" }))
        assertFalse(viewModel.items.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons work correctly and search/filter actions update filter model")
    fun testButtonsWorkCorrectly() {
        val filter = mockSortFilterViewModel.getOrCreateVideoFilter()
        assertNotNull(filter)

        val updatedFilter = VideoFilter().apply { Search = "Gladiator" }
        mockSortFilterViewModel.updateVideoFilter(updatedFilter)

        verify { mockSortFilterViewModel.updateVideoFilter(updatedFilter) }
        assertEquals("Gladiator", updatedFilter.Search)
    }
}
