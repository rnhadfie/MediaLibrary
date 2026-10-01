package com.example.medialibrary.utils.TriStateCheckBoxHelper

import android.content.Context
import android.graphics.Color
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.medialibrary.R
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SetupSortTriStateCheckBoxTests {
    private lateinit var context: Context
    private lateinit var containerView: FrameLayout
    private lateinit var labelTextView: TextView
    private lateinit var button: Button

    private val testLabelResId = R.string.asc

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        labelTextView = TextView(context).apply { id = R.id.label_text }
        button = Button(context).apply { id = R.id.triStateButton }

        containerView = FrameLayout(context).apply {
            addView(labelTextView)
            addView(button)
        }

        TriStateCheckBoxHelper.currentState = null
    }

    @Test
    fun setupSortWithNullInitialValue() {
        TriStateCheckBoxHelper.setupSortTriStateCheckBox(containerView, testLabelResId, null) {}

        assertEquals("", button.text.toString())
        assertEquals(Color.BLACK, button.currentTextColor)
        assertNull(button.tag)
    }

    @Test
    fun clickCyclesSortStatesCorrectly() {
        TriStateCheckBoxHelper.currentState = false

        TriStateCheckBoxHelper.setupSortTriStateCheckBox(containerView, testLabelResId, null) {}

        // 1st Click: null -> true
        button.performClick()
        assertEquals(true, button.tag)
        assertEquals(Color.BLACK, button.currentTextColor)

        // 2nd Click: true -> false
        button.performClick()
        assertEquals(false, button.tag)

        // 3rd Click: false -> null
        button.performClick()
        assertNull(button.tag)
        assertEquals("", button.text.toString())
    }
}
