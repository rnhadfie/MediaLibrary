package com.example.medialibrary.book

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.book.ui.collecting.BookCollectingViewModel
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class BookCollectingFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModelInitialization() {
        val viewModel = BookCollectingViewModel()
        assertNotNull(viewModel)
    }
}
