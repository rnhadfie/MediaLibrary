package com.example.medialibrary.book

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.book.ui.collecting.CollectingBookViewModel
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class CollectingBookFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModelInitialization() {
        val viewModel = CollectingBookViewModel()
        assertNotNull(viewModel)
    }
}
