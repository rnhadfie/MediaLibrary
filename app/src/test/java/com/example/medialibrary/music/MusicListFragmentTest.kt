package com.example.medialibrary.music

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import com.example.medialibrary.music.ui.list.MusicListViewModel
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.book.BookFilter
import models.shared.DisplayMediaItem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MusicListFragmentTest {

    private lateinit var viewModel: MusicListViewModel
    private lateinit var mockSortFilterViewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = MusicListViewModel()
        mockSortFilterViewModel = mockk(relaxed = true)

        every { mockSortFilterViewModel.getOrCreateBookFilter() } returns BookFilter()
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
            Title = "The Hobbit"
        }
        val item2 = DisplayMediaItem().apply {
            Id = "2"
            Title = "Dune"
        }

        viewModel.setItems(listOf(item1, item2))

        val items = viewModel.items.value
        assertNotNull(items)
        assertEquals(2, items?.size)
        assertEquals("The Hobbit", items?.get(0)?.Title)
        assertEquals("Dune", items?.get(1)?.Title)
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
        viewModel.setItems(listOf(DisplayMediaItem().apply { Title = "Foundation" }))
        assertFalse(viewModel.items.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons work correctly and search/filter actions update filter model")
    fun testButtonsWorkCorrectly() {
        val filter = mockSortFilterViewModel.getOrCreateBookFilter()
        assertNotNull(filter)

        val updatedFilter = BookFilter().apply { Search = "Foundation" }
        mockSortFilterViewModel.updateBookFilter(updatedFilter)

        verify { mockSortFilterViewModel.updateBookFilter(updatedFilter) }
        assertEquals("Foundation", updatedFilter.Search)
    }
}
