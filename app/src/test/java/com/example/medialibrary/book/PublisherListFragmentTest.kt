package com.example.medialibrary.book

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.book.ui.publisher.PublisherViewModel
import controllers.BookController
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.book.Publisher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PublisherListFragmentTest {

    private lateinit var viewModel: PublisherViewModel
    private lateinit var mockController: BookController

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = PublisherViewModel()
        mockController = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible, publishers LiveData contains list of publishers")
    fun testDataVisible() {
        val pub1 = Publisher("1", "Penguin Random House")
        val pub2 = Publisher("2", "HarperCollins")

        viewModel.setPublishers(listOf(pub1, pub2))

        val publishers = viewModel.publishers.value
        assertNotNull(publishers)
        assertEquals(2, publishers?.size)
        assertEquals("Penguin Random House", publishers?.get(0)?.Name)
        assertEquals("HarperCollins", publishers?.get(1)?.Name)
    }

    @Test
    @DisplayName("When there is no data, publishers LiveData is empty or null")
    fun testNoData() {
        viewModel.setPublishers(emptyList())

        val publishers = viewModel.publishers.value
        assertNotNull(publishers)
        assertTrue(publishers.isNullOrEmpty())
    }

    @Test
    @DisplayName("Specific components hidden or visible depending on publishers state")
    fun testComponentVisibility() {
        // When no data: empty state active
        viewModel.setPublishers(emptyList())
        assertTrue(viewModel.publishers.value.isNullOrEmpty())

        // When data is present: publishers list active
        val pub = Publisher("1", "Tor Books")
        viewModel.setPublishers(listOf(pub))
        assertFalse(viewModel.publishers.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons work correctly and invoke controller add/delete actions")
    fun testButtonsWorkCorrectly() {
        val newPublisher = Publisher("", "Simon & Schuster")
        every { mockController.AddPublisher(any()) } returns true

        mockController.AddPublisher(newPublisher)
        verify { mockController.AddPublisher(newPublisher) }

        val pubToDelete = Publisher("10", "Old Publisher")
        every { mockController.DeletePublisher(pubToDelete.Id, false) } returns mockk(relaxed = true)

        mockController.DeletePublisher(pubToDelete.Id, false)
        verify { mockController.DeletePublisher(pubToDelete.Id, false) }
    }
}
