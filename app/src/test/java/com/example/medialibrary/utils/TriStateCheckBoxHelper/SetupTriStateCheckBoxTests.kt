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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SetupTriStateCheckBoxTests {
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
    fun setupWithNullInitialValue() {
        TriStateCheckBoxHelper.setupTriStateCheckBox(containerView, testLabelResId, null) {}

        assertFalse(containerView.isFocusable)
        assertTrue(containerView.isClickable)
        assertEquals("☐", button.text.toString())
        assertEquals(Color.BLACK, button.currentTextColor)
        assertNull(button.tag)
    }

    @Test
    fun setupWithTrueInitialValue() {
        TriStateCheckBoxHelper.setupTriStateCheckBox(containerView, testLabelResId, true) {}

        assertEquals("✓", button.text.toString())
        assertEquals(Color.GREEN, button.currentTextColor)
        assertEquals(true, button.tag)
    }

    @Test
    fun setupWithFalseInitialValue() {
        TriStateCheckBoxHelper.setupTriStateCheckBox(containerView, testLabelResId, false) {}

        assertEquals("✗", button.text.toString())
        assertEquals(Color.RED, button.currentTextColor)
        assertEquals(false, button.tag)
    }

    @Test
    fun setupWithNullContainer() {
        TriStateCheckBoxHelper.setupTriStateCheckBox(null, testLabelResId, null) {}
    }

    @Test
    fun clickCyclesStatesCorrectly() {
        var lambdaCalledCount = 0
        val onStateChanged: (Boolean?) -> Unit = { lambdaCalledCount++ }

        TriStateCheckBoxHelper.setupTriStateCheckBox(containerView, testLabelResId, null, onStateChanged)

        // 1st Click: null -> true
        button.performClick()
        assertEquals("✓", button.text.toString())
        assertEquals(Color.GREEN, button.currentTextColor)
        assertEquals(true, button.tag)
        assertEquals(1, lambdaCalledCount)

        // 2nd Click: true -> false
        button.performClick()
        assertEquals("✗", button.text.toString())
        assertEquals(Color.RED, button.currentTextColor)
        assertEquals(false, button.tag)
        assertEquals(2, lambdaCalledCount)

        // 3rd Click: false -> null (Testing through label click)
        labelTextView.performClick()
        assertEquals("☐", button.text.toString())
        assertEquals(Color.BLACK, button.currentTextColor)
        assertNull(button.tag)
        assertEquals(3, lambdaCalledCount)
    }
}
