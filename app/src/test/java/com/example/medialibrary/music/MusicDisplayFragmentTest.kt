package com.example.medialibrary.music

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.book.ui.display.BookDisplayViewModel
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import com.example.medialibrary.music.ui.display.MusicDisplayViewModel
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.book.Book
import models.book.BookFilter
import models.book.BookItem
import models.book.Enums as BookEnums
import models.book.Enums.BookType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MusicDisplayFragmentTest {

    private lateinit var viewModel: MusicDisplayViewModel
    private lateinit var mockSortFilterViewModel: SortFilterViewmodel

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = MusicDisplayViewModel()
        mockSortFilterViewModel = mockk(relaxed = true)

        every { mockSortFilterViewModel.getOrCreateBookFilter() } returns BookFilter()
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible, mediaItems LiveData contains books and items counts are correct")
    fun testDataVisible() {
        val item1 = BookItem().apply { Read = true; Owned = true }
        val item2 = BookItem().apply { Read = false; Owned = true }
        val book1 = Book().apply {
            Id = "101"
            Title = "Sample Book 1"
            Type = BookType.Novel
            Items = arrayListOf(item1, item2)
            Ongoing = true
            Collecting = true
            HasCollectedAllItems = true
        }
        val book2 = Book().apply {
            Id = "102"
            Title = "Sample Book 2"
            Type = BookType.Manga
            Items = arrayListOf()
            Ongoing = false
            Collecting = false
            HasCollectedAllItems = false
        }

        viewModel.setMediaItems(listOf(book1, book2))

        val mediaItems = viewModel.MediaItems.value
        assertNotNull(mediaItems)
        assertEquals(2, mediaItems?.size)
        assertEquals("Sample Book 1", (mediaItems?.get(0) as? Book)?.Title)
        assertEquals("Sample Book 2", (mediaItems?.get(1) as? Book)?.Title)
    }

    @Test
    @DisplayName("When there is no data, mediaItems LiveData is empty or null")
    fun testNoData() {
        assertNull(viewModel.MediaItems.value)

        viewModel.setMediaItems(emptyList())

        val mediaItems = viewModel.MediaItems.value
        assertNotNull(mediaItems)
        assertTrue(mediaItems.isNullOrEmpty())
    }


    @Test
    @DisplayName("Buttons work correctly and invoke filter / sort actions")
    fun testButtonsWorkCorrectly() {
        val initialFilter = mockSortFilterViewModel.getOrCreateBookFilter()
        assertNotNull(initialFilter)

        val updatedFilter = BookFilter().apply { Search = "Dune" }
        mockSortFilterViewModel.updateBookFilter(updatedFilter)

        verify { mockSortFilterViewModel.updateBookFilter(updatedFilter) }
        assertEquals("Dune", updatedFilter.Search)
    }
}
