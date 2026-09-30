package com.example.medialibrary.other

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.other.ui.collecting.OtherCollectingViewModel
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class OtherCollectingFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModelInitialization() {
        val viewModel = OtherCollectingViewModel()
        assertNotNull(viewModel)
    }
}
