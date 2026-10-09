package com.example.medialibrary.music

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.example.medialibrary.book.ui.form.BookFormViewModel
import com.example.medialibrary.music.ui.form.MusicFormViewModel
import controllers.BookController
import controllers.MusicController
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import models.book.Book
import models.book.BookItem
import models.book.BookSetup
import models.book.Enums as BookEnums
import models.book.Publisher
import models.music.Enums
import models.music.Music
import models.music.MusicSetup
import models.shared.Tag
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MusicFormFragmentTest {

    private lateinit var viewModel: MusicFormViewModel
    private lateinit var mockController: MusicController

    @BeforeEach
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })

        viewModel = MusicFormViewModel()
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
        val existingBook = Music().apply {
            Id = "10"
            Title = "Dune"
            Collecting = true
            MusicGenre = Enums.MusicGenre.Electronic
            Ongoing = true
            Tag = "12"
        }

        val setup = MusicSetup().apply {
            Tags = arrayListOf(Tag().apply { Id = "12"; Name = "Sci-Fi" })
        }

        every { mockController.GetMusic("10") } returns existingBook

        viewModel.loadCd("10", mockController)

        val loadedBook = viewModel.music.value
        assertNotNull(loadedBook)
        assertEquals("Dune", loadedBook?.Title)
        assertTrue(loadedBook?.Collecting == true)
        assertEquals(Enums.MusicGenre.Electronic, loadedBook?.MusicGenre)
        assertTrue(loadedBook?.Ongoing == true)
        assertEquals("12", loadedBook?.Tag)

    }

    @Test
    @DisplayName("When there is no data in new mode, initial form state has default values and validation catches missing title")
    fun testNoDataNewMode() {
        val initialBook = viewModel.music.value
        assertNotNull(initialBook)
        assertTrue(initialBook?.Title.isNullOrEmpty())

        // Validation fails when title is empty
        val validationErrors = viewModel.validate()
        assertTrue(validationErrors.isNotEmpty())

        // Validation passes when title is filled
        viewModel.updateTitle("The Hobbit")
        val updatedErrors = viewModel.validate()
        assertTrue(updatedErrors.isEmpty())
    }



    @Test
    @DisplayName("Buttons work correctly and invoke form action handlers")
    fun testButtonsWorkCorrectly() {
        // Test updating title and fields
        viewModel.updateTitle("Foundation")
        viewModel.updateTagName("Classic")

        // Test toggles
        viewModel.toggleCollecting(true)
        assertTrue(viewModel.music.value?.Collecting == true)

        viewModel.toggleCollectionComplete(true)
        assertTrue(viewModel.music.value?.HasCollectedAllItems == true)


        // Test getSaveObject
        val saveObj = viewModel.getSaveObject()
        assertNotNull(saveObj)
        assertEquals("Foundation", saveObj.Music?.Title)
        assertEquals("Classic", saveObj.NewTag)
    }
}
