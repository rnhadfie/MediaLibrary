package com.example.medialibrary.music

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.music.ui.collecting.MusicCollectingViewModel
import models.shared.DisplayMediaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class MusicCollectingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testSetItems_updatesLiveData() {
        val viewModel = MusicCollectingViewModel()
        val item = DisplayMediaItem().apply {
            Id = "10"
            Title = "Collecting Series"
        }

        viewModel.setItems(listOf(item))

        val result = viewModel.items.value
        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals("Collecting Series", result?.get(0)?.Title)
    }
}
