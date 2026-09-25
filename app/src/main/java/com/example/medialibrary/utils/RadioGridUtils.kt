package com.example.medialibrary.utils

import android.widget.RadioButton
import android.widget.TableLayout
import android.widget.TableRow


object RadioGridUtils {

     fun populateRadioGridFromMap(
        tableLayout: TableLayout,
        optionsMap: Map<Int, String>,
        columnCount: Int,
        selectedId: Int? = null,
        onSelectionChanged: (selectedId: Int) -> Unit
    ) {
        // Use the raw context from the table layout
        val context = tableLayout.context
        tableLayout.removeAllViews()

        var currentlySelectedButton: RadioButton? = null
        val rowsData = optionsMap.entries.chunked(columnCount)

        for (rowData in rowsData) {
            // 1. TableRow must use TableLayout.LayoutParams
            val tableRow = TableRow(context).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            }

            for (entry in rowData) {
                val itemId = entry.key
                val itemText = entry.value

                // 2. Pass the standard context directly here
                val radioButton = RadioButton(context).apply {
                    this.text = itemText
                    this.tag = itemId

                    // 3. FIXED CRASH HERE:
                    // RadioButton is a child of TableRow, so it MUST use TableRow.LayoutParams.
                    // We explicitly pass the width (0), height (WRAP_CONTENT), and weight (1f).
                    this.layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f
                    )

                    if (selectedId != null && itemId == selectedId) {
                        this.isChecked = true
                        currentlySelectedButton = this
                    }

                    setOnClickListener { view ->
                        val clickedRadio = view as RadioButton
                        if (currentlySelectedButton != null && currentlySelectedButton != clickedRadio) {
                            currentlySelectedButton?.isChecked = false
                        }
                        currentlySelectedButton = clickedRadio
                        val clickedId = clickedRadio.tag as Int
                        onSelectionChanged(clickedId)
                    }
                }

                tableRow.addView(radioButton)
            }

            tableLayout.addView(tableRow)
        }
    }

    fun setSelection(tableLayout: TableLayout, targetId: Int) {
        var clickedRadioToSelect: RadioButton? = null
        val buttonsToUncheck = mutableListOf<RadioButton>()

        // 1. Loop through TableRows inside TableLayout
        for (i in 0 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as? TableRow ?: continue

            // 2. Loop through RadioButtons inside TableRow
            for (j in 0 until row.childCount) {
                val radioButton = row.getChildAt(j) as? RadioButton ?: continue
                val itemId = radioButton.tag as? Int

                if (itemId == targetId) {
                    clickedRadioToSelect = radioButton
                } else if (radioButton.isChecked) {
                    buttonsToUncheck.add(radioButton)
                }
            }
        }

        // 3. Uncheck old selections first, then check the target selection
        // We do this at the end to prevent flickering or weird event bugs
        buttonsToUncheck.forEach { it.isChecked = false }
        clickedRadioToSelect?.isChecked = true
    }

    /**
     * Programmatically unchecks all radio buttons inside the grid.
     */
    fun clearSelection(tableLayout: TableLayout) {
        for (i in 0 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as? TableRow ?: continue
            for (j in 0 until row.childCount) {
                val radioButton = row.getChildAt(j) as? RadioButton ?: continue
                radioButton.isChecked = false
            }
        }
    }

}