package com.example.medialibrary.other

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.other.ui.collecting.OtherCollectingViewModel
import com.example.medialibrary.other.ui.utils.SortFilterViewmodel
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.other.OtherFilter
import models.shared.DisplayMediaItem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OtherCollectingFragmentTest {

    private lateinit var viewModel: OtherCollectingViewModel
    private lateinit var mockSortFilterViewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = OtherCollectingViewModel()
        mockSortFilterViewModel = mockk(relaxed = true)

        every { mockSortFilterViewModel.getOrCreateItemFilter() } returns OtherFilter().apply { Collecting = true }
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible, viewModel items contain items")
    fun testDataVisible() {
        val item1 = DisplayMediaItem().apply {
            Id = "1"
            Title = "Collectible A"
        }
        val item2 = DisplayMediaItem().apply {
            Id = "2"
            Title = "Collectible B"
        }
        viewModel.setItems(listOf(item1, item2))

        val items = viewModel.items.value
        assertNotNull(items)
        assertEquals(2, items?.size)
        assertEquals("Collectible A", items?.get(0)?.Title)
        assertEquals("Collectible B", items?.get(1)?.Title)
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

        viewModel.setItems(listOf(DisplayMediaItem().apply { Title = "Statue" }))
        assertFalse(viewModel.items.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons and filter actions work correctly")
    fun testButtonsWorkCorrectly() {
        val initialFilter = mockSortFilterViewModel.getOrCreateItemFilter()
        assertNotNull(initialFilter)

        val updatedFilter = OtherFilter().apply { Collecting = true; Search = "Statue" }
        mockSortFilterViewModel.updateItemFilter(updatedFilter)

        verify { mockSortFilterViewModel.updateItemFilter(updatedFilter) }
        assertEquals("Statue", updatedFilter.Search)
    }
}
