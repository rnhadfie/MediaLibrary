package com.example.medialibrary.other

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medialibrary.R
import com.example.medialibrary.other.ui.otherform.OtherFormFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtherFormFragmentTest {
    @Test
    fun testOtherFormFragmentLaunchesAndDisplaysViews() {
        val scenario = launchFragmentInContainer<OtherFormFragment>(
            themeResId = R.style.Theme_MediaLibrary
        )
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.edit_other_title)).check(matches(isDisplayed()))
        onView(withId(R.id.button_save_other)).check(matches(isDisplayed()))
    }
}
