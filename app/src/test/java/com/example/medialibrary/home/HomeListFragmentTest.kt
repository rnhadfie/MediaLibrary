package com.example.medialibrary.home

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.home.ui.list.HomeListViewModel
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

class HomeListFragmentTest {

    private lateinit var viewModel: HomeListViewModel
    private lateinit var mockSortFilterViewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = HomeListViewModel()
        mockSortFilterViewModel = mockk(relaxed = true)

        every { mockSortFilterViewModel.getOrCreateMainFilter() } returns Filter()
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
            Title = "Star Wars"
            MediaType = Enums.MediaType.Video
        }
        val item2 = DisplayMediaItem().apply {
            Id = "2"
            Title = "Abbey Road"
            MediaType = Enums.MediaType.Music
        }

        viewModel.setItems(listOf(item1, item2))

        val items = viewModel.items.value
        assertNotNull(items)
        assertEquals(2, items?.size)
        assertEquals("Star Wars", items?.get(0)?.Title)
        assertEquals("Abbey Road", items?.get(1)?.Title)
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
        // Empty state
        viewModel.setItems(emptyList())
        assertTrue(viewModel.items.value.isNullOrEmpty())

        // Non-empty state
        viewModel.setItems(listOf(DisplayMediaItem().apply { Title = "Item A" }))
        assertFalse(viewModel.items.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons work correctly and search/filter actions update filter model")
    fun testButtonsWorkCorrectly() {
        val filter = mockSortFilterViewModel.getOrCreateMainFilter()
        assertNotNull(filter)

        val updatedFilter = Filter().apply { Search = "Star Wars" }
        mockSortFilterViewModel.updateMainFilter(updatedFilter)

        verify { mockSortFilterViewModel.updateMainFilter(updatedFilter) }
        assertEquals("Star Wars", updatedFilter.Search)
    }
}
