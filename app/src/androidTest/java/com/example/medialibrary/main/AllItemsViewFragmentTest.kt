package com.example.medialibrary.main

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medialibrary.R
import com.example.medialibrary.home.ui.all_list.AllItemsViewFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AllItemsViewFragmentTest {

    @Test
    fun testFragmentLaunchesAndDisplaysViews() {
        val scenario = launchFragmentInContainer<AllItemsViewFragment>(
            themeResId = R.style.Theme_MediaLibrary
        )
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.recyclerview_transform)).check(matches(isDisplayed()))
        onView(withId(R.id.search_view)).check(matches(isDisplayed()))
        onView(withId(R.id.button_filter)).check(matches(isDisplayed()))
    }
}
