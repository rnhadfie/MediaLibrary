package com.example.medialibrary.book

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.book_list.BookListFragment
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class BookListFragmentTest {
    @Test
    fun testFragmentLaunchesAndDisplaysViews() {
        // Launch the fragment in an isolated graphical container
        val scenario = launchFragmentInContainer<BookListFragment>(
            themeResId = R.style.Theme_MediaLibrary // Ensure your app's theme or a valid parent theme is applied
        )
        scenario.moveToState(Lifecycle.State.RESUMED)

        // Verify key UI elements are displayed on launch
        Espresso.onView(ViewMatchers.withId(R.id.recyclerview_books))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.search_view))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.display_filter_btn))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testSearchView_submitsQuery_updatesUI() {
        val scenario = launchFragmentInContainer<BookListFragment>(themeResId = R.style.Theme_MediaLibrary)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // Type text into the SearchView and submit it
        Espresso.onView(ViewMatchers.withId(R.id.search_view))
            .perform(
                ViewActions.click(),
                ViewActions.typeText("Fantasy Book"),
                ViewActions.pressImeActionButton()
            )

        // Verify the active filter layout summary appears or changes state
        Espresso.onView(ViewMatchers.withId(R.id.display_active_filter_card))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testFilterButton_opensBottomSheetAndApplies() {
        val scenario = launchFragmentInContainer<BookListFragment>(themeResId = R.style.Theme_MediaLibrary)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // Click the main filter button to open the BottomSheetDialog
        Espresso.onView(ViewMatchers.withId(R.id.display_filter_btn)).perform(ViewActions.click())

        // Verify that the bottom sheet view elements are now visible on the screen
        Espresso.onView(ViewMatchers.withId(R.id.button_sheet_fitler_book))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.button_sheet_clear_book))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Click apply filter within the bottom sheet
        Espresso.onView(ViewMatchers.withId(R.id.button_sheet_fitler_book))
            .perform(ViewActions.click())

        // Verify dialog closes and main view is still there
        Espresso.onView(ViewMatchers.withId(R.id.recyclerview_books))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testBookItemListClick_copiesToClipboard() {
        val scenario = launchFragmentInContainer<BookListFragment>(themeResId = R.style.Theme_MediaLibrary)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // Click the view intended to trigger the clipboard copy action
        Espresso.onView(ViewMatchers.withId(R.id.display_copy_list_btn)).perform(ViewActions.click())

        // Verify data was successfully copied to the system ClipboardManager
        val context = ApplicationProvider.getApplicationContext<Context>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        TestCase.assertTrue("Clipboard should contain text data", clipboard.hasPrimaryClip())
        TestCase.assertEquals(
            "Primary clip description type should be text/plain",
            ClipDescription.MIMETYPE_TEXT_PLAIN,
            clipboard.primaryClipDescription?.getMimeType(0)
        )
    }
}