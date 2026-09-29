package com.example.medialibrary.music

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.book.ui.collecting.CollectingBookViewModel
import com.example.medialibrary.music.ui.collecting.MusicCollectingViewModel
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class MusicCollectingFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModelInitialization() {
        val viewModel = MusicCollectingViewModel()
        assertNotNull(viewModel)
    }
}
