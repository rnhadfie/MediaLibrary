package com.example.medialibrary.video

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import models.book.Book
import com.example.medialibrary.video.ui.display.VideoDisplayViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class VideoDisplayViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInitialState() {
        val viewModel = VideoDisplayViewModel()
        assertEquals("Graphs go here", viewModel.text.value)
        assertNull(viewModel.MediaItems.value)
    }

    @Test
    fun testSetMediaItems_updatesLiveData() {
        val viewModel = VideoDisplayViewModel()
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

        val items = viewModel.MediaItems.value
        assertNotNull(items)
        assertEquals(2, items?.size)
        assertEquals("Sample Book 1", (items?.get(0) as? Book)?.Title)
        assertEquals("Sample Book 2", (items?.get(1) as? Book)?.Title)
    }
}
