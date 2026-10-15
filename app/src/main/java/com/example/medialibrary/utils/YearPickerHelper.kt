package com.example.medialibrary.utils

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.NumberPicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

object YearPickerHelper {

    fun showYearPickerDialog(
        context: Context,
        initialYear: Int? = null,
        title: String = "Select Year",
        minYear: Int = 1800,
        maxYear: Int = Calendar.getInstance().get(Calendar.YEAR) + 10,
        onYearSelected: (Int) -> Unit
    ) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val defaultYear = when {
            initialYear != null && initialYear in minYear..maxYear -> initialYear
            else -> currentYear
        }

        val numberPicker = NumberPicker(context).apply {
            minValue = minYear
            maxValue = maxYear
            value = defaultYear
            wrapSelectorWheel = false
        }

        val container = FrameLayout(context).apply {
            setPadding(64, 32, 64, 32)
            addView(
                numberPicker,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(container)
            .setPositiveButton("OK") { dialog, _ ->
                onYearSelected(numberPicker.value)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
