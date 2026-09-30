package com.example.medialibrary.book

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import models.book.Book
import com.example.medialibrary.book.ui.display.BookDisplayViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class BookDisplayViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInitialState() {
        val viewModel = BookDisplayViewModel()
        assertEquals("Graphs go here", viewModel.text.value)
        assertNull(viewModel.mediaItems.value)
    }

    @Test
    fun testSetMediaItems_updatesLiveData() {
        val viewModel = BookDisplayViewModel()
        val book1 = Book().apply {
            Id = "101"
            Title = "Sample Book 1"
        }
        val book2 = Book().apply {
            Id = "102"
            Title = "Sample Book 2"
        }
        val books = listOf(book1, book2)

        viewModel.setMediaItems(books)

        val items = viewModel.mediaItems.value
        assertNotNull(items)
        assertEquals(2, items?.size)
        assertEquals("Sample Book 1", (items?.get(0) as? Book)?.Title)
        assertEquals("Sample Book 2", (items?.get(1) as? Book)?.Title)
    }
}
