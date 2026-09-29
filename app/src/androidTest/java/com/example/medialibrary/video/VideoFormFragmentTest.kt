package com.example.medialibrary.video

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medialibrary.R
import com.example.medialibrary.video.ui.form.VideoFormFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoFormFragmentTest {
    @Test
    fun testVideoFormFragmentLaunchesAndDisplaysViews() {
        val scenario = launchFragmentInContainer<VideoFormFragment>(
            themeResId = R.style.Theme_MediaLibrary
        )
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.title_input)).check(matches(isDisplayed()))
        onView(withId(R.id.save_btn)).check(matches(isDisplayed()))
    }
}
