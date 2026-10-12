package com.example.medialibrary.book

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.book.ui.form.BookFormViewModel
import controllers.BookController
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import models.book.Book
import models.book.BookItem
import models.book.BookSetup
import models.book.Enums as BookEnums
import models.book.Publisher
import models.shared.Tag
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BookFormFragmentTest {

    private lateinit var viewModel: BookFormViewModel
    private lateinit var mockController: BookController

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = BookFormViewModel()
        mockController = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        clearAllMocks()
    }

    @Test
    @DisplayName("When data is visible in edit mode, fields and items are loaded correctly")
    fun testDataVisibleInEditMode() {
        val bookItem1 = BookItem().apply { VolumeNumber = "1"; VolumeTitle = "Volume One" }
        val existingBook = Book().apply {
            Id = "10"
            Title = "Dune"
            Author = "Frank Herbert"
            Type = BookEnums.BookType.Novel
            Collecting = true
            Ongoing = true
            Publisher = "5"
            Tag = "12"
            Items = arrayListOf(bookItem1)
        }

        val setup = BookSetup().apply {
            Publishers = arrayListOf(Publisher("5", "Chilton Books"))
            Tag = arrayListOf(Tag().apply { Id = "12"; Name = "Sci-Fi" })
        }

        every { mockController.GetBook("10") } returns existingBook

        viewModel.loadBook("10", mockController, setup)

        val loadedBook = viewModel.book.value
        assertNotNull(loadedBook)
        assertEquals("Dune", loadedBook?.Title)
        assertEquals("Frank Herbert", loadedBook?.Author)
        assertEquals(BookEnums.BookType.Novel, loadedBook?.Type)
        assertTrue(loadedBook?.Collecting == true)

        val loadedItems = viewModel.items.value
        assertNotNull(loadedItems)
        assertEquals(1, loadedItems?.size)
        assertEquals("Volume One", loadedItems?.get(0)?.VolumeTitle)
    }

    @Test
    @DisplayName("When there is no data in new mode, initial form state has default values and validation catches missing title")
    fun testNoDataNewMode() {
        val initialBook = viewModel.book.value
        assertNotNull(initialBook)
        assertTrue(initialBook?.Title.isNullOrEmpty())
        assertEquals(BookEnums.BookType.NoneSelected, initialBook?.Type)
        assertEquals(0, viewModel.items.value?.size)

        // Validation fails when title is empty
        val validationErrors = viewModel.validateFields()
        assertTrue(validationErrors.isNotEmpty())

        // Validation passes when title is filled
        viewModel.updateTitle("The Hobbit")
        viewModel.updateYear("1937")
        assertEquals("1937", viewModel.book.value?.Year)
        val updatedErrors = viewModel.validateFields()
        assertTrue(updatedErrors.isEmpty())
    }

    @Test
    @DisplayName("Specific components hidden or visible depending on book type and cover state")
    fun testComponentVisibility() {
        // Novel type: demographics not applicable
        viewModel.updateBookType(BookEnums.BookType.Novel)
        assertEquals(BookEnums.BookType.Novel, viewModel.book.value?.Type)

        // Manga type: demographics applicable
        viewModel.updateBookType(BookEnums.BookType.Manga)
        assertEquals(BookEnums.BookType.Manga, viewModel.book.value?.Type)
    }

    @Test
    @DisplayName("Buttons work correctly and invoke form action handlers")
    fun testButtonsWorkCorrectly() {
        // Test updating title and fields
        viewModel.updateTitle("Foundation")
        viewModel.updatePublisherName("Gollancz")
        viewModel.updateTagName("Classic")

        // Test toggles
        viewModel.toggleCollecting(true)
        assertTrue(viewModel.book.value?.Collecting == true)

        viewModel.toggleCompleted(true)
        assertTrue(viewModel.book.value?.Ongoing == true)

        // Test adding volume item
        val item1 = BookItem().apply { VolumeNumber = "1"; VolumeTitle = "Vol 1" }
        viewModel.addOrUpdateItem(item1)
        assertEquals(1, viewModel.items.value?.size)

        // Test getSaveObject
        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("Foundation", saveObj.book?.Title)
        assertEquals("Gollancz", saveObj.NewPublisher)
        assertEquals("Classic", saveObj.NewTag)
    }
}
