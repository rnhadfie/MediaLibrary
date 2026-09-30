package com.example.medialibrary.video

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.video.ui.collecting.CollectingViewModel
import models.shared.DisplayMediaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class CollectingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testSetItems_updatesLiveData() {
        val viewModel = CollectingViewModel()
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
