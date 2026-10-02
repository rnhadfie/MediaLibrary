package com.example.medialibrary.video

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.home.ui.collecting.HomeCollectingViewModel
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class HomeCollectingFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModelInitialization() {
        val viewModel = HomeCollectingViewModel()
        assertNotNull(viewModel)
    }
}
