package com.example.medialibrary.video

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.video.ui.display.VideoDisplayViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VideoDisplayFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModelInitialization() {
        val viewModel = VideoDisplayViewModel()
        assertEquals("Graphs go here", viewModel.text.value)
    }
}
