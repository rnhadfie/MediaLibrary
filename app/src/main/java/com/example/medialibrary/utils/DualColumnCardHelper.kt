package com.example.medialibrary.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        when(mediaType)
        {
            Enums.MediaType.Book -> {
                binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.section_book_light))
            }
            Enums.MediaType.Music -> {
                binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.section_music_light))
            }
            Enums.MediaType.Video -> {
                binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.section_video_light))
            }

            Enums.MediaType.Other -> {
                binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.section_other_light))
            }
            else -> {}
        }
        val col1 = binding.dualCardColumnOne
        val col2 = binding.dualCardColumnTwo
        val emptyState = binding.emptyStateContainer

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