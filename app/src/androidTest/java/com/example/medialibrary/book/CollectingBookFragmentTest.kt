package com.example.medialibrary.book

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.book_list.BookListFragment
import com.example.medialibrary.book.ui.collecting.CollectingBookFragment
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectingBookFragmentTest {


    @Test
    fun testCollectingBookFragmentLaunchesAndDisplaysViews() {
        val scenario = launchFragmentInContainer<CollectingBookFragment>(
            themeResId = R.style.Theme_MediaLibrary
        )
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.recyclerview_books)).check(matches(isDisplayed()))
    }

    @Test
    fun testSearchView_submitsQuery_updatesUI() {
        val scenario =
            launchFragmentInContainer<CollectingBookFragment>(themeResId = R.style.Theme_MediaLibrary)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // Type text into the SearchView and submit it
        Espresso.onView(ViewMatchers.withId(R.id.search_view))
            .perform(
                ViewActions.click(),
                ViewActions.typeText("Fantasy Book"),
                ViewActions.pressImeActionButton()
            )

        // Verify the active filter layout summary appears or changes state
        Espresso.onView(ViewMatchers.withId(R.id.active_filter_card))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testFilterButton_opensBottomSheetAndApplies() {
        val scenario =
            launchFragmentInContainer<CollectingBookFragment>(themeResId = R.style.Theme_MediaLibrary)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // Click the main filter button to open the BottomSheetDialog
        Espresso.onView(ViewMatchers.withId(R.id.filter_btn)).perform(ViewActions.click())

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
    fun testFilterButton_opensBookBottomSheet() {
        val scenario =
            launchFragmentInContainer<BookListFragment>(themeResId = R.style.Theme_MediaLibrary)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // Click the main filter button to open the BottomSheetDialog
        Espresso.onView(ViewMatchers.withId(R.id.filter_btn)).perform(ViewActions.click())

        // Verify that the bottom sheet view elements are now visible on the screen

        //Checkboxes
        Espresso.onView(ViewMatchers.withId(R.id.collected))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.read))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.reading))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.anyItemsOwned))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.standaloneOrSeriesComplete))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //Dropdowns
        Espresso.onView(ViewMatchers.withId(R.id.book_type_autocomplete))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.format_autocomplete))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.pub_autocomplete_input))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.genre_autocomplete))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.tag_autocomplete))
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
        val scenario =
            launchFragmentInContainer<CollectingBookFragment>(themeResId = R.style.Theme_MediaLibrary)
        scenario.moveToState(Lifecycle.State.RESUMED)

        // Click the view intended to trigger the clipboard copy action
        Espresso.onView(ViewMatchers.withId(R.id.copy_list_btn)).perform(ViewActions.click())

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
