package com.example.medialibrary.book

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.form.BookFormFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookFormFragmentTest {

    @Test
    fun testBookFormFragmentLaunchesAndDisplaysViews() {
        val scenario = launchFragmentInContainer<BookFormFragment>(
            themeResId = R.style.Theme_MediaLibrary
        )
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.input_title_field)).check(matches(isDisplayed()))
        onView(withId(R.id.input_author_text)).check(matches(isDisplayed()))
        onView(withId(R.id.input_artist_text)).check(matches(isDisplayed()))
        onView(withId(R.id.form_save_btn)).check(matches(isDisplayed()))
    }
}
