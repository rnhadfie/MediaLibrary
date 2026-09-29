package com.example.medialibrary.book

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.book.ui.book_list.BookListViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class BookListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testSetItems_updatesLiveData() {
        val viewModel = BookListViewModel()
        val item1 = DisplayMediaItem().apply {
            Id = "1"
            Title = "Book One"
        }
        val item2 = DisplayMediaItem().apply {
            Id = "2"
            Title = "Book Two"
        }
        val itemList = listOf(item1, item2)

        viewModel.setItems(itemList)

        val result = viewModel.items.value
        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals("Book One", result?.get(0)?.Title)
        assertEquals("Book Two", result?.get(1)?.Title)
    }
}
