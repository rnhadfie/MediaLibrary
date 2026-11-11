package com.example.medialibrary.utils

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import com.example.medialibrary.R
import com.example.medialibrary.databinding.ViewDualColumnCardBinding
import models.shared.Enums

object DualColumnCardHelper {
    fun setupDualColumnCard(
        map: Map<String, Int>,
        binding: ViewDualColumnCardBinding,
        emptyStateText: Int,
        mediaType: Enums.MediaType,
        context: Context)
    {
        val cardBgColor = ThemeUtils.getThemeColor(context, com.google.android.material.R.attr.colorSecondary)
        binding.cardView.setCardBackgroundColor(cardBgColor)

        val col1 = binding.dualCardColumnOne
        val col2 = binding.dualCardColumnTwo
        val emptyState = binding.emptyStateContainer

        if(map.isEmpty())
        {
            col1.visibility = View.GONE
            col2.visibility = View.GONE
            emptyState.root.visibility = View.VISIBLE
            emptyState.emptyStateText.setText(emptyStateText)
            val themePrimary = ThemeUtils.getThemeColor(context, androidx.appcompat.R.attr.colorPrimary)
            emptyState.emptyStateText.setTextColor(themePrimary)
            emptyState.emptyStateIcon.imageTintList = ColorStateList.valueOf(themePrimary)
        }
        else {

            col1.visibility = View.VISIBLE
            col2.visibility = View.VISIBLE
            emptyState.root.visibility = View.GONE

            val sortedMap = map.toList()
                .sortedByDescending { (_, value) -> value } // Sort list by the value
                .toMap()

            val halfSize = (sortedMap.size + 1) / 2
            val chunks = sortedMap.entries.chunked(halfSize)

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