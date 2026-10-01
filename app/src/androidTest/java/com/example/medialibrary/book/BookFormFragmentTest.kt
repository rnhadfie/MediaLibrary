package com.example.medialibrary.book

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medialibrary.BaseFormFragment
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.form.BookFormFragment
import com.example.medialibrary.databinding.BookFragmentFormBinding
import controllers.BookController
import models.book.Book
import models.book.BookFilter
import models.book.BookItem
import models.book.BookSaveObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import repository.database.MediaLibraryDbHelper

@RunWith(AndroidJUnit4::class)
class BookFormFragmentTest {

    private lateinit var context: Context
    private lateinit var dbHelper: MediaLibraryDbHelper
    private lateinit var controller: BookController

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbHelper = MediaLibraryDbHelper(context)
        controller = BookController(dbHelper)
    }

    private fun getFormBinding(fragment: BookFormFragment): BookFragmentFormBinding {
        val method = BaseFormFragment::class.java.getDeclaredMethod("getBinding")
        method.isAccessible = true
        return method.invoke(fragment) as BookFragmentFormBinding
    }

    @Test
    fun testActivityTitle_dependsOnIsEdit() {
        // Add Mode (isEdit = false)
        val addIntent = Intent(context, BookFormActivity::class.java).apply {
            putExtra("EXTRA_ID", "-1")
            putExtra("EXTRA_IS_EDIT", false)
        }
        ActivityScenario.launch<BookFormActivity>(addIntent).use { scenario ->
            scenario.onActivity { activity ->
                assertEquals("Add Book", activity.supportActionBar?.title.toString())
            }
        }

        // Edit Mode (isEdit = true)
        val editIntent = Intent(context, BookFormActivity::class.java).apply {
            putExtra("EXTRA_ID", "1")
            putExtra("EXTRA_IS_EDIT", true)
        }
        ActivityScenario.launch<BookFormActivity>(editIntent).use { scenario ->
            scenario.onActivity { activity ->
                assertEquals("Edit Book", activity.supportActionBar?.title.toString())
            }
        }
    }

    @Test
    fun testDataLoaded_whenValidIdProvided() {
        // First insert a test book into database
        val testBook = Book().apply {
            Title = "Dune"
            Author = "Frank Herbert"
            Artist = "Sam Weber"
        }
        val saveObject = BookSaveObject().apply {
            book = testBook
        }
        controller.AddBook(saveObject)

        // Get the inserted book's ID
        val insertedBook = controller.GetBooks(BookFilter()).firstOrNull { it.Title == "Dune" }
        assertNotNull(insertedBook)
        val bookId = insertedBook!!.Id

        val intent = Intent(context, BookFormActivity::class.java).apply {
            putExtra("EXTRA_ID", bookId)
            putExtra("EXTRA_IS_EDIT", true)
        }

        ActivityScenario.launch<BookFormActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.main) as? BookFormFragment
                assertNotNull(fragment)
                val binding = getFormBinding(fragment!!)

                assertEquals("Dune", binding.titleInput.text.toString())
                assertEquals("Frank Herbert", binding.authorInput.text.toString())
                assertEquals("Sam Weber", binding.artistInput.text.toString())
            }
        }
    }

    @Test
    fun testItemListDisplayed_whenItemsExist() {
        val testBook = Book().apply {
            Title = "The Lord of the Rings"
            Items = mutableListOf(
                BookItem().apply { VolumeNumber = "1"; VolumeTitle = "The Fellowship of the Ring" },
                BookItem().apply { VolumeNumber = "2"; VolumeTitle = "The Two Towers" }
            )
        }
        val saveObject = BookSaveObject().apply {
            book = testBook
        }
        controller.AddBook(saveObject)

        val insertedBook = controller.GetBooks(BookFilter()).firstOrNull { it.Title == "The Lord of the Rings" }
        assertNotNull(insertedBook)
        val bookId = insertedBook!!.Id

        val intent = Intent(context, BookFormActivity::class.java).apply {
            putExtra("EXTRA_ID", bookId)
            putExtra("EXTRA_IS_EDIT", true)
        }

        ActivityScenario.launch<BookFormActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.main) as? BookFormFragment
                assertNotNull(fragment)
                val binding = getFormBinding(fragment!!)

                val adapter = binding.recyclerBookItems.adapter
                assertNotNull(adapter)
                assertEquals(2, adapter!!.itemCount)
            }
        }
    }

    @Test
    fun testBottomSheetOpens_whenAddVolumeClicked() {
        val intent = Intent(context, BookFormActivity::class.java).apply {
            putExtra("EXTRA_ID", "-1")
            putExtra("EXTRA_IS_EDIT", false)
        }

        ActivityScenario.launch<BookFormActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.main) as? BookFormFragment
                assertNotNull(fragment)
                val binding = getFormBinding(fragment!!)

                binding.addItemBtn.performClick()
            }
            onView(withId(R.id.save_item_btn)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        }
    }

    @Test
    fun testErrorDisplayed_whenTitleInputIsEmpty() {
        val intent = Intent(context, BookFormActivity::class.java).apply {
            putExtra("EXTRA_ID", "-1")
            putExtra("EXTRA_IS_EDIT", false)
        }

        ActivityScenario.launch<BookFormActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.main) as? BookFormFragment
                assertNotNull(fragment)
                val binding = getFormBinding(fragment!!)

                binding.titleInput.setText("")
                binding.saveBtn.performClick()

                assertEquals("Title is required", binding.titleLabel.error.toString())
            }
        }
    }
}
