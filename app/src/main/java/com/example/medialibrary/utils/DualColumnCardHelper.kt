package com.example.medialibrary.utils

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.medialibrary.R
import com.example.medialibrary.databinding.ViewEmptyStateBinding

object DualColumnCardHelper {
    fun setupDualColumnCard(
        map: Map<String, Int>,
        col1: LinearLayout,
        col2: LinearLayout,
        emptyState: ViewEmptyStateBinding,
        emptyStateText: Int,
        context: Context)
    {
        if(map.isEmpty())
        {
            col1.visibility = View.GONE
            col2.visibility = View.GONE
            emptyState.root.visibility = View.VISIBLE
            emptyState.emptyStateText.setText(emptyStateText)
        }
        else {

            col1.visibility = View.VISIBLE
            col2.visibility = View.VISIBLE
            emptyState.root.visibility = View.GONE

            val publisherInformationSortedMap = map.toList()
                .sortedByDescending { (_, value) -> value } // Sort list by the value
                .toMap()

            val halfSize = (publisherInformationSortedMap.size + 1) / 2
            val chunks = publisherInformationSortedMap.entries.chunked(halfSize)

            val firstHalf = chunks.getOrNull(0)?.associate { it.key to it.value } ?: emptyMap()
            val secondHalf = chunks.getOrNull(1)?.associate { it.key to it.value } ?: emptyMap()

            col1.removeAllViews()
            col2.removeAllViews()

            for ((key, value) in firstHalf) {
                val textView = TextView(context)
                textView.text = context.getString(R.string.dual_card_text, key, value)
                textView.setPadding(8, 8, 8, 8)
                col1.addView(textView)
            }
            for ((key, value) in secondHalf) {
                val textView = TextView(context)
                textView.text = context.getString(R.string.dual_card_text, key, value)
                textView.setPadding(8, 8, 8, 8)
                col2.addView(textView)
            }
        }
    }
}