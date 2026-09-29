package com.example.medialibrary.other

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.backend.models.book.BookItem
import com.example.medialibrary.backend.models.book.Enums as BookEnums
import com.example.medialibrary.backend.models.book.Publisher
import com.example.medialibrary.backend.models.other.OtherItem
import com.example.medialibrary.backend.models.shared.GenreObject
import com.example.medialibrary.backend.models.shared.Tag
import com.example.medialibrary.book.ui.form.BookFormViewModel
import com.example.medialibrary.other.ui.otherform.OtherFormViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class OtherFormViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInitialBookState() {
        val viewModel = OtherFormViewModel()
        val other = viewModel.other.value

        assertNotNull(other)
        assertEquals(0, viewModel.items.value?.size)
    }

    @Test
    fun testUpdateFields() {
        val viewModel = OtherFormViewModel()

        viewModel.updateTitle("Dune")
        assertEquals("Dune", viewModel.other.value?.Title)

    }

    @Test
    fun testUpdateTag() {
        val viewModel = OtherFormViewModel()

        val existingTag = Tag().apply {
            Id = "12"
            Name = "Sci-Fi"
        }
        viewModel.updateTag(existingTag)
        assertEquals("12", viewModel.other.value?.Tag)

        viewModel.updateTagName("New SciFi Tag")
        assertEquals("0", viewModel.other.value?.Tag)
    }

    @Test
    fun testToggleTogglesAndGenres() {
        val viewModel = OtherFormViewModel()

        viewModel.toggleCollecting(true)
        assertTrue(viewModel.other.value?.Collecting == true)

        viewModel.toggleCompleted(true)
        assertTrue(viewModel.other.value?.Ongoing == true)

        viewModel.toggleCollectionComplete(true)
        assertTrue(viewModel.other.value?.HasCollectedAllItems == true)


    }

    @Test
    fun testAddUpdateAndDeleteItems() {
        val viewModel = OtherFormViewModel()

        val item1 = OtherItem().apply {
            Title = "Volume 1"
        }
        val item2 = OtherItem().apply {
            Title = "Volume 2"
        }

        viewModel.addOrUpdateItem(item1)
        assertEquals(1, viewModel.items.value?.size)

        viewModel.addOrUpdateItem(item2)
        assertEquals(2, viewModel.items.value?.size)

        val updatedItem1 = OtherItem().apply {
            Title = "Volume 1 Updated"
        }
        viewModel.addOrUpdateItem(updatedItem1, position = 0)
        assertEquals(2, viewModel.items.value?.size)
        assertEquals("Volume 1 Updated", viewModel.items.value?.get(0)?.Title)

        viewModel.deleteItem(0)
        assertEquals(1, viewModel.items.value?.size)
        assertEquals("Volume 2", viewModel.items.value?.get(0)?.Title)
    }

    @Test
    fun testValidation() {
        val viewModel = OtherFormViewModel()

        assertNotNull(viewModel.validateFields()) // Title is required

        viewModel.updateTitle("The Hobbit")
        assertEquals(0, viewModel.validateFields().size)
    }

    @Test
    fun testGetSaveObject() {
        val viewModel = OtherFormViewModel()
        viewModel.updateTitle("Foundation")
        viewModel.updateTagName("Classic")

        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("Foundation", saveObj.Other?.Title)
        assertEquals("Classic", saveObj.NewTag)
    }
}
