package com.example.medialibrary.book

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.display.BookDisplayFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDisplayFragmentTest {

    @Test
    fun testBookDisplayFragmentLaunchesAndDisplaysViews() {
        val scenario = launchFragmentInContainer<BookDisplayFragment>(
            themeResId = R.style.Theme_MediaLibrary
        )
        scenario.moveToState(Lifecycle.State.RESUMED)

        Espresso.onView(ViewMatchers.withId(R.id.top_controls_container))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}