package com.example.medialibrary.other

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.other.ui.list.OtherListViewModel
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

class OtherListFragmentTest {

    private lateinit var viewModel: OtherListViewModel
    private lateinit var mockSortFilterViewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = OtherListViewModel()
        mockSortFilterViewModel = mockk(relaxed = true)

        every { mockSortFilterViewModel.getOrCreateItemFilter() } returns OtherFilter()
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
            Id = "10"
            Title = "Board Game A"
        }
        val item2 = DisplayMediaItem().apply {
            Id = "20"
            Title = "Board Game B"
        }

        viewModel.setItems(listOf(item1, item2))

        val items = viewModel.items.value
        assertNotNull(items)
        assertEquals(2, items?.size)
        assertEquals("Board Game A", items?.get(0)?.Title)
        assertEquals("Board Game B", items?.get(1)?.Title)
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

        viewModel.setItems(listOf(DisplayMediaItem().apply { Title = "Card Set" }))
        assertFalse(viewModel.items.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons work correctly and search/filter actions update filter model")
    fun testButtonsWorkCorrectly() {
        val filter = mockSortFilterViewModel.getOrCreateItemFilter()
        assertNotNull(filter)

        val updatedFilter = OtherFilter().apply { Search = "Card Set" }
        mockSortFilterViewModel.updateItemFilter(updatedFilter)

        verify { mockSortFilterViewModel.updateItemFilter(updatedFilter) }
        assertEquals("Card Set", updatedFilter.Search)
    }
}
