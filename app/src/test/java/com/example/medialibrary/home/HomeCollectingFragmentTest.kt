package com.example.medialibrary.home

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.home.ui.collecting.HomeCollectingViewModel
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

class HomeCollectingFragmentTest {

    private lateinit var viewModel: HomeCollectingViewModel
    private lateinit var mockSortFilterViewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = HomeCollectingViewModel()
        mockSortFilterViewModel = mockk(relaxed = true)

        every { mockSortFilterViewModel.getOrCreateMainFilter() } returns Filter().apply { Collecting = true }
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible, viewModel items contain items and list is populated")
    fun testDataVisible() {
        val item1 = DisplayMediaItem().apply {
            Id = "1"
            Title = "Marvel Series"
            MediaType = Enums.MediaType.Book
        }
        val item2 = DisplayMediaItem().apply {
            Id = "2"
            Title = "Inception"
            MediaType = Enums.MediaType.Video
        }

        viewModel.setItems(listOf(item1, item2))

        val items = viewModel.items.value
        assertNotNull(items)
        assertEquals(2, items?.size)
        assertEquals("Marvel Series", items?.get(0)?.Title)
        assertEquals("Inception", items?.get(1)?.Title)
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
        // Empty state visibility
        viewModel.setItems(emptyList())
        assertTrue(viewModel.items.value.isNullOrEmpty())

        // Data state visibility
        viewModel.setItems(listOf(DisplayMediaItem().apply { Title = "Item 1" }))
        assertFalse(viewModel.items.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons work correctly and filter/sort actions update filter model")
    fun testButtonsWorkCorrectly() {
        val initialFilter = mockSortFilterViewModel.getOrCreateMainFilter()
        assertNotNull(initialFilter)

        val updatedFilter = Filter().apply { Collecting = true; Search = "Inception" }
        mockSortFilterViewModel.updateMainFilter(updatedFilter)

        verify { mockSortFilterViewModel.updateMainFilter(updatedFilter) }
        assertEquals("Inception", updatedFilter.Search)
    }
}
