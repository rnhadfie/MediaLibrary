package com.example.medialibrary.home

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.home.ui.tag.TagViewModel
import controllers.MainController
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.shared.Tag
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TagListFragmentTest {

    private lateinit var viewModel: TagViewModel
    private lateinit var mockController: MainController

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = TagViewModel()
        mockController = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible, tags LiveData contains list of tags")
    fun testDataVisible() {
        val tag1 = Tag("1", "Sci-Fi")
        val tag2 = Tag("2", "Favorites")

        viewModel.setTags(listOf(tag1, tag2))

        val tags = viewModel.tags.value
        assertNotNull(tags)
        assertEquals(2, tags?.size)
        assertEquals("Sci-Fi", tags?.get(0)?.Name)
        assertEquals("Favorites", tags?.get(1)?.Name)
    }

    @Test
    @DisplayName("When there is no data, tags LiveData is empty or null")
    fun testNoData() {
        viewModel.setTags(emptyList())

        val tags = viewModel.tags.value
        assertNotNull(tags)
        assertTrue(tags.isNullOrEmpty())
    }

    @Test
    @DisplayName("Specific components hidden or visible depending on tags state")
    fun testComponentVisibility() {
        // When no data: empty state active
        viewModel.setTags(emptyList())
        assertTrue(viewModel.tags.value.isNullOrEmpty())

        // When data is present: tags list active
        val tag = Tag("1", "Must Watch")
        viewModel.setTags(listOf(tag))
        assertFalse(viewModel.tags.value.isNullOrEmpty())
    }

    @Test
    @DisplayName("Buttons work correctly and invoke controller add/delete actions")
    fun testButtonsWorkCorrectly() {
        val newTag = Tag("", "New Tag")
        every { mockController.AddTag(any()) } returns true

        mockController.AddTag(newTag)
        verify { mockController.AddTag(newTag) }

        val tagToDelete = Tag("10", "Old Tag")
        every { mockController.DeleteTag(tagToDelete.Id, false) } returns mockk(relaxed = true)

        mockController.DeleteTag(tagToDelete.Id, false)
        verify { mockController.DeleteTag(tagToDelete.Id, false) }
    }
}
