package com.example.medialibrary.music

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.music.ui.display.MusicDisplayViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MusicDisplayFragmentUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModelInitialization() {
        val viewModel = MusicDisplayViewModel()
        assertEquals("Graphs go here", viewModel.text.value)
    }
}
