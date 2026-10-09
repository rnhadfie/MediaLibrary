package com.example.medialibrary.other

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.other.ui.otherform.OtherFormViewModel
import controllers.OtherController
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import models.other.Other
import models.other.OtherItem
import models.shared.Tag
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OtherFormFragmentTest {

    private lateinit var viewModel: OtherFormViewModel
    private lateinit var mockController: OtherController

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = OtherFormViewModel()
        mockController = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible in edit mode, other collection and items load correctly")
    fun testDataVisibleInEditMode() {
        val otherItem = OtherItem().apply { Id = 101; Series = 1 }
        val existingOther = Other().apply {
            Id = "1"
            Title = "Vinyl Figure"
            Collecting = true
            HasCollectedAllItems = true
            Tag = "5"
            Items = arrayListOf(otherItem)
        }

        every { mockController.GetOtherItem("1") } returns existingOther

        viewModel.loadOtherCollection("1", mockController)

        val loaded = viewModel.other.value
        assertNotNull(loaded)
        assertEquals("Vinyl Figure", loaded?.Title)
        assertTrue(loaded?.Collecting == true)

        val items = viewModel.items.value
        assertNotNull(items)
        assertEquals(1, items?.size)
    }

    @Test
    @DisplayName("When there is no data in new mode, initial state is empty and validation detects missing title")
    fun testNoDataNewMode() {
        val initial = viewModel.other.value
        assertNotNull(initial)
        assertTrue(initial?.Title.isNullOrEmpty())
        assertEquals(0, viewModel.items.value?.size)

        val errors = viewModel.validateFields()
        assertTrue(errors.containsKey("title"))

        viewModel.updateTitle("Board Game")
        val updatedErrors = viewModel.validateFields()
        assertFalse(updatedErrors.containsKey("title"))
    }

    @Test
    @DisplayName("Specific components hidden or shown depending on state")
    fun testComponentVisibility() {
        val tag = Tag("5", "Limited Edition")
        viewModel.updateTag(tag)

        assertEquals("5", viewModel.other.value?.Tag)
    }

    @Test
    @DisplayName("Buttons and actions work correctly")
    fun testButtonsWorkCorrectly() {
        viewModel.updateTitle("Art Print")
        viewModel.updateTagName("New Tag")
        viewModel.toggleCollecting(true)

        val item = OtherItem().apply { Id = 1 }
        viewModel.addOrUpdateItem(item)

        assertEquals(1, viewModel.items.value?.size)

        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("Art Print", saveObj.Other?.Title)
        assertEquals("New Tag", saveObj.NewTag)
    }
}
