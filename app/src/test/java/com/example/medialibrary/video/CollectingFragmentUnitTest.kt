package com.example.medialibrary.video

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.home.ui.collecting.CollectingViewModel
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class CollectingFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModelInitialization() {
        val viewModel = CollectingViewModel()
        assertNotNull(viewModel)
    }
}
