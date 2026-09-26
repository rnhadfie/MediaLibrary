package com.example.medialibrary.book

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.backend.models.book.Publisher
import com.example.medialibrary.book.ui.publisher.PublisherViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class PublisherViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testSetPublishers_updatesLiveData() {
        val viewModel = PublisherViewModel()
        val pub1 = Publisher().apply {
            Id = 1
            Name = "Penguin"
        }
        val pub2 = Publisher().apply {
            Id = 2
            Name = "HarperCollins"
        }

        viewModel.setPublishers(listOf(pub1, pub2))

        val publishers = viewModel.publishers.value
        assertNotNull(publishers)
        assertEquals(2, publishers?.size)
        assertEquals("Penguin", publishers?.get(0)?.Name)
        assertEquals("HarperCollins", publishers?.get(1)?.Name)
    }
}
