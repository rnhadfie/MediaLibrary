package com.example.medialibrary.book

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medialibrary.BaseFormFragment
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.book_list.BookListFragment
import com.example.medialibrary.book.ui.form.BookFormFragment
import com.example.medialibrary.databinding.BookFragmentFormBinding
import androidx.test.espresso.assertion.ViewAssertions.matches
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import androidx.test.espresso.matcher.ViewMatchers.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class BookListFragmentTest {

    private fun getFormBinding(fragment: BookFormFragment): BookFragmentFormBinding {
        val method = BaseFormFragment::class.java.getDeclaredMethod("getBinding")
        method.isAccessible = true
        return method.invoke(fragment) as BookFragmentFormBinding
    }

    fun betterScrollTo(): ViewAction {
        return actionWithAssertions(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                    isDescendantOfA(anyOf(
                        isAssignableFrom(ScrollView::class.java),
                        isAssignableFrom(HorizontalScrollView::class.java),
                        isAssignableFrom(NestedScrollView::class.java) // Adds support for nested scroll
                    ))
                )
            }

            override fun getDescription(): String = "scroll to view inside a scroll container"

            override fun perform(uiController: UiController, view: View) {
                if (isDisplayingAtLeast(90).matches(view)) {
                    return // Already visible
                }
                val rect = android.graphics.Rect()
                view.getDrawingRect(rect)
                view.requestRectangleOnScreen(rect, true)
                uiController.loopMainThreadUntilIdle()
            }
        })
    }



    @Test
    fun testFragmentLaunchesAndDisplaysViews() {
        // Launch the fragment in an isolated graphical container
        val scenario = launchFragmentInContainer<BookListFragment>(
            themeResId = R.style.Theme_MediaLibrary // Ensure your app's theme or a valid parent theme is applied
        )
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.search_view))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.filter_btn))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.sort_btn))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
    /*
        @Test
        fun testSearchView_submitsQuery_updatesUI() {
            val scenario =
                launchFragmentInContainer<BookListFragment>(themeResId = R.style.Theme_MediaLibrary)
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
                launchFragmentInContainer<BookListFragment>(themeResId = R.style.Theme_MediaLibrary)
            scenario.moveToState(Lifecycle.State.RESUMED)

            // Click the main filter button to open the BottomSheetDialog
            Espresso.onView(ViewMatchers.withId(R.id.filter_btn)).perform(betterScrollTo(), ViewActions.click())

            // Verify that the bottom sheet view elements are now visible on the screen
            Espresso.onView(ViewMatchers.withId(R.id.button_sheet_fitler_book))
                .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            Espresso.onView(ViewMatchers.withId(R.id.button_sheet_clear_book))
                .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

            // Click apply filter within the bottom sheet
            Espresso.onView(ViewMatchers.withId(R.id.button_sheet_fitler_book))
                .perform(scrollTo(), ViewActions.click())

            // Verify dialog closes and main view is still there
            Espresso.onView(ViewMatchers.withId(R.id.recyclerview_books))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }*/
/*
    @Test
    fun testFilterButton_opensBookBottomSheet() {
        val scenario =
            launchFragmentInContainer<BookListFragment>(themeResId = R.style.Theme_MediaLibrary)
        scenario.moveToState(Lifecycle.State.RESUMED)

        Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard())


        // Click the main filter button to open the BottomSheetDialog
        Espresso.onView(ViewMatchers.withId(R.id.filter_btn)).perform(ViewActions.click())

        // Verify that the bottom sheet view elements are now visible on the screen

        //Checkboxes
        /*
        Espresso.onView(ViewMatchers.withId(R.id.collected))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.read))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.reading))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.anyItemsOwned))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.collecting))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.standaloneOrSeriesComplete))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

        //Dropdowns
        Espresso.onView(ViewMatchers.withId(R.id.book_type_autocomplete))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.format_autocomplete))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.pub_autocomplete_input))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.genre_autocomplete))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.tag_autocomplete))
            .check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
*/
        // Checkboxes
        onView(withId(R.id.collected)).check(matches(isDisplayed()))
        onView(withId(R.id.read)).check(matches(isDisplayed()))
        onView(withId(R.id.reading)).check(matches(isDisplayed()))
        onView(withId(R.id.anyItemsOwned)).check(matches(isDisplayed()))
        onView(withId(R.id.collecting)).check(matches(isDisplayed()))
        onView(withId(R.id.standaloneOrSeriesComplete)).check(matches(isDisplayed()))

        /*
        // Dropdowns
        onView(withId(R.id.book_type_autocomplete)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.format_autocomplete)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.pub_autocomplete_input)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.genre_autocomplete)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withId(R.id.tag_autocomplete)).inRoot(isDialog()).check(matches(isDisplayed())) */

        // Click apply filter within the bottom sheet
        Espresso.onView(ViewMatchers.withId(R.id.button_sheet_fitler_book))
            .perform(betterScrollTo(), ViewActions.click())

        // Verify dialog closes and main view is still there
        Espresso.onView(ViewMatchers.withId(R.id.recyclerview_books))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }*/

    @Test
    fun testBookItemListClick_copiesToClipboard() {
        val scenario =
            launchFragmentInContainer<BookListFragment>(themeResId = R.style.Theme_MediaLibrary)
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