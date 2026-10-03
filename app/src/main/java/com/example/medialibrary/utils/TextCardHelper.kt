package com.example.medialibrary.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.medialibrary.R
import com.example.medialibrary.databinding.ViewEmptyStateBinding
import com.example.medialibrary.databinding.ViewTextCardBinding
import models.shared.Enums

object TextCardHelper {
    fun setupTextCard(
        map: Map<String, Int>,
        binding: ViewTextCardBinding,
        emptyState: ViewEmptyStateBinding,
        emptyStateText: Int,
        MediaType: Enums.MediaType,
        context: Context)
    {
        val layout = binding.textColumn

        when(MediaType)
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

        if(map.isEmpty())
        {
            layout.visibility = View.GONE
            emptyState.root.visibility = View.VISIBLE
            emptyState.emptyStateText.setText(emptyStateText)
        }
        else {

            layout.visibility = View.VISIBLE
            emptyState.root.visibility = View.GONE

            val sortedMap = map.toList()
                .sortedByDescending { (_, value) -> value } // Sort list by the value
                .toMap()

            layout.removeAllViews()

            for ((key, value) in sortedMap) {
                val textView = TextView(context)
                textView.text = context.getString(R.string.dual_card_text, key, value)
                textView.setPadding(8, 8, 8, 8)
                layout.addView(textView)
            }
        }
    }
}