package com.example.medialibrary.book

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.medialibrary.book.ui.form.BookFormViewModel
import models.book.BookItem
import models.book.Publisher
import models.shared.GenreObject
import models.shared.Tag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import models.book.Enums as BookEnums

class BookFormViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInitialBookState() {
        val viewModel = BookFormViewModel()
        val book = viewModel.book.value

        assertNotNull(book)
        assertEquals(BookEnums.BookType.NoneSelected, book?.Type)
        assertNotNull(book?.Genre)
        assertEquals(0, viewModel.items.value?.size)
    }

    @Test
    fun testUpdateFields() {
        val viewModel = BookFormViewModel()

        viewModel.updateTitle("Dune")
        assertEquals("Dune", viewModel.book.value?.Title)

        viewModel.updateAuthor("Frank Herbert")
        assertEquals("Frank Herbert", viewModel.book.value?.Author)

        viewModel.updateArtist("Artist X")
        assertEquals("Artist X", viewModel.book.value?.Artist)

        viewModel.updateBookType(BookEnums.BookType.Novel)
        assertEquals(BookEnums.BookType.Novel, viewModel.book.value?.Type)
    }

    @Test
    fun testUpdatePublisherAndTag() {
        val viewModel = BookFormViewModel()

        val existingPublisher = Publisher().apply {
            Id = "5"
            Name = "Chilton Books"
        }
        viewModel.updatePublisher(existingPublisher)
        assertEquals("5", viewModel.book.value?.Publisher)

        val newPublisher = Publisher().apply {
            Id = "-1"
            Name = "New Self Publisher"
        }
        viewModel.updatePublisher(newPublisher)
        assertEquals("-1", viewModel.book.value?.Publisher)

        viewModel.updatePublisherName("Custom Publisher")
        assertEquals("", viewModel.book.value?.Publisher)

        val existingTag = Tag().apply {
            Id = "12"
            Name = "Sci-Fi"
        }
        viewModel.updateTag(existingTag)
        assertEquals("12", viewModel.book.value?.Tag)

        viewModel.updateTagName("New SciFi Tag")
        assertEquals("", viewModel.book.value?.Tag)
    }

    @Test
    fun testToggleTogglesAndGenres() {
        val viewModel = BookFormViewModel()

        viewModel.toggleCollecting(true)
        assertTrue(viewModel.book.value?.Collecting == true)

        viewModel.toggleCompleted(true)
        assertTrue(viewModel.book.value?.Ongoing == true)

        viewModel.toggleCollectionComplete(true)
        assertTrue(viewModel.book.value?.HasCollectedAllItems == true)

        val genreObj = GenreObject().apply {
            genreId = 1
            genreName = "Fantasy"
        }
        viewModel.toggleGenre(genreObj)
        assertTrue(viewModel.selectedGenres.value?.contains(genreObj) == true)
        assertTrue(viewModel.book.value?.Genre?.contains(1) == true)

        viewModel.toggleGenre(genreObj)
        assertFalse(viewModel.selectedGenres.value?.contains(genreObj) == true)
        assertFalse(viewModel.book.value?.Genre?.contains(1) == true)
    }

    @Test
    fun testAddUpdateAndDeleteItems() {
        val viewModel = BookFormViewModel()

        val item1 = BookItem().apply {
            VolumeNumber = "1"
            VolumeTitle = "Volume 1"
        }
        val item2 = BookItem().apply {
            VolumeNumber = "2"
            VolumeTitle = "Volume 2"
        }

        viewModel.addOrUpdateItem(item1)
        assertEquals(1, viewModel.items.value?.size)

        viewModel.addOrUpdateItem(item2)
        assertEquals(2, viewModel.items.value?.size)

        val updatedItem1 = BookItem().apply {
            VolumeNumber = "1"
            VolumeTitle = "Volume 1 Updated"
        }
        viewModel.addOrUpdateItem(updatedItem1, position = 0)
        assertEquals(2, viewModel.items.value?.size)
        assertEquals("Volume 1 Updated", viewModel.items.value?.get(0)?.VolumeTitle)

        viewModel.deleteItem(0)
        assertEquals(1, viewModel.items.value?.size)
        assertEquals("Volume 2", viewModel.items.value?.get(0)?.VolumeTitle)
    }

    @Test
    fun testValidation() {
        val viewModel = BookFormViewModel()

        assertNotNull(viewModel.validateFields()) // Title is required

        viewModel.updateTitle("The Hobbit")
        assertEquals(0, viewModel.validateFields().size)
    }

    @Test
    fun testGetSaveObject() {
        val viewModel = BookFormViewModel()
        viewModel.updateTitle("Foundation")
        viewModel.updatePublisherName("Gollancz")
        viewModel.updateTagName("Classic")

        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("Foundation", saveObj.book?.Title)
        assertEquals("Gollancz", saveObj.NewPublisher)
        assertEquals("Classic", saveObj.NewTag)
    }
}
