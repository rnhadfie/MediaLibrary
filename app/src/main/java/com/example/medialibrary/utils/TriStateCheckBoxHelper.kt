package com.example.medialibrary.utils

import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.medialibrary.R

object TriStateCheckBoxHelper {

  var currentState: Boolean? = null

    private fun updateButtonText(button: Button, onStateChanged: (Boolean?) -> Unit)
    {
        when (button.tag) {
            null -> {
                button.text = "✓"
                button.setTextColor(Color.GREEN)
                button.tag = true
                onStateChanged(currentState)
            }
            true -> {
                button.text = "✗"
                button.setTextColor(Color.RED)
                button.tag = false
                onStateChanged(currentState)
            }
            false -> {
                button.text = "☐"
                button.setTextColor(Color.BLACK)
                button.tag = null
                onStateChanged(currentState)
            }
        }
    }

    private fun updateSortButtonText(button: Button, onStateChanged: (Boolean?) -> Unit)
    {
        when (button.tag) {
            null -> {
                button.text = "ASC"
                button.setTextColor(Color.BLACK)
                button.tag = true
                onStateChanged(currentState)
            }
            true -> {
                button.text = "DESC"
                button.setTextColor(Color.BLACK)

                button.tag = false
                onStateChanged(currentState)
            }
            false -> {
                button.text = ""
                button.setTextColor(Color.BLACK)
                button.tag = null
                onStateChanged(currentState)
            }
        }
    }
    fun setupTriStateCheckBox(
        containerView: View?,
        labelResId: Int,
        initialValue: Boolean?,
        onStateChanged: (Boolean?) -> Unit
    ) {
        if (containerView == null) return

        containerView.isFocusable = false
        containerView.isClickable = true

        val labelTextView = containerView.findViewById<TextView>(R.id.label_text)
        labelTextView.setText(labelResId)
        val button = containerView.findViewById<Button>(R.id.triStateButton)
        button.tag = initialValue;
        when (initialValue) {
            null -> {
                button.text = "☐"
                button.setTextColor(Color.BLACK)
            }
            true -> {
                button.text = "✓"
                button.setTextColor(Color.GREEN)
            }
            false -> {
                button.text = "✗"
                button.setTextColor(Color.RED)
            }
        }

        labelTextView.setOnClickListener {
            updateButtonText(button, onStateChanged)
        }
        button.setOnClickListener {
            updateButtonText(button, onStateChanged)
        }

    }

    fun setupSortTriStateCheckBox(
        containerView: View?,
        labelResId: Int,
        initialValue: Boolean?,
        onStateChanged: (Boolean?) -> Unit
    ) {
        if (containerView == null) return

        containerView.isFocusable = false
        containerView.isClickable = true

        val labelTextView = containerView.findViewById<TextView>(R.id.label_text)
        labelTextView.setText(labelResId)
        val button = containerView.findViewById<Button>(R.id.triStateButton)
        button.tag = initialValue;
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        when (initialValue) {
            null -> {
                button.text = ""
                button.setTextColor(Color.BLACK)
            }
            true -> {
                button.text = "ASC"
                button.setTextColor(Color.BLACK)
            }
            false -> {
                button.text = "DESC"
                button.setTextColor(Color.BLACK)
            }
        }

        labelTextView.setOnClickListener {
            updateSortButtonText(button, onStateChanged)
        }
        button.setOnClickListener {
            updateSortButtonText(button, onStateChanged)
        }

    }

}
