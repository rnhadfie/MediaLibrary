package com.example.medialibrary.book

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.book.ui.display.BookDisplayViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BookDisplayFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModelInitialization() {
        val viewModel = BookDisplayViewModel()
        assertEquals("Graphs go here", viewModel.text.value)
    }
}
