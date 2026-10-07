package com.example.medialibrary.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.medialibrary.R
import com.example.medialibrary.databinding.ViewEmptyStateBinding
import com.example.medialibrary.databinding.ViewTextCardBinding
import com.example.medialibrary.databinding.ViewTextCardDisplayBinding
import models.shared.Enums

object TextCardHelper {
    fun setupTextCard(
        valueMap: Map<String, Any>,
        extraValueMap: Map<String, Any>?,
        binding: ViewTextCardBinding,
        emptyState: ViewEmptyStateBinding,
        emptyStateText: Int,
        textLabel: Int,
        valueLabel: Int,
        valueExtraLabel: Int,
        mediaType: Enums.MediaType,
        layoutInflater: LayoutInflater,
        context: Context)
    {
        val layout = binding.textColumn

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

        if(valueMap.isEmpty())
        {
            layout.visibility = View.GONE
            emptyState.root.visibility = View.VISIBLE
            emptyState.emptyStateText.setText(emptyStateText)
        }
        else {

            layout.visibility = View.VISIBLE
            emptyState.root.visibility = View.GONE


            layout.removeAllViews()

            val header = ViewTextCardDisplayBinding.inflate(layoutInflater)
            if(textLabel > -1) {
                header.tableText.setText(textLabel)
            }
            else{
                header.tableText.text = ""
            }
            if(valueLabel > -1) {
                header.tableValue.setText(valueLabel)
            }
            else {
                header.tableValue.text = ""
            }
            if(valueExtraLabel > -1) {
                header.tableValueExtra.setText(valueExtraLabel)
            }
            else { header.tableValueExtra.text = ""}
            if(extraValueMap== null)
            {
                header.textDivider1.visibility = View.GONE
            }

            layout.addView(header.root)


            for ((key, value) in valueMap) {
                val textView = ViewTextCardDisplayBinding.inflate(layoutInflater)
                textView.tableText.text = key
                textView.tableValue.text = value.toString()
                if(extraValueMap!= null && extraValueMap.containsKey(key))
                {
                    textView.tableValueExtra.text = extraValueMap[key].toString()
                }
                else {
                    textView.tableValueExtra.text = ""
                    textView.textDivider1.visibility = View.GONE
                }
                layout.addView(textView.root)
            }
        }
    }
}