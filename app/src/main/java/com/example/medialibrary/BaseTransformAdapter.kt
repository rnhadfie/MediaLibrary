package com.example.medialibrary

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import models.shared.DisplayMediaItem
import models.shared.Enums
import com.example.medialibrary.databinding.ItemTransformBinding
import com.example.medialibrary.utils.SaveEditDeleteUtils

open class BaseTransformAdapter : ListAdapter<DisplayMediaItem, BaseTransformAdapter.TransformViewHolder>(DIFF_CALLBACK) {

    private data class SectionColors(
        val main: Int,
        val light: Int,
        val dark: Int,
        val iconRes: Int
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {
        val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.item = item
        holder.binding.executePendingBindings()

        val context = holder.itemView.context

        val colors = when (item.MediaType) {
            Enums.MediaType.Book -> SectionColors(
                ContextCompat.getColor(context, R.color.section_book),
                ContextCompat.getColor(context, R.color.section_book_light),
                ContextCompat.getColor(context, R.color.section_book_dark),
                R.drawable.ic_book_small
            )
            Enums.MediaType.Video -> SectionColors(
                ContextCompat.getColor(context, R.color.section_video),
                ContextCompat.getColor(context, R.color.section_video_light),
                ContextCompat.getColor(context, R.color.section_video_dark),
                R.drawable.ic_movie
            )
            Enums.MediaType.Music -> SectionColors(
                ContextCompat.getColor(context, R.color.section_music),
                ContextCompat.getColor(context, R.color.section_music_light),
                ContextCompat.getColor(context, R.color.section_music_dark),
                R.drawable.ic_music
            )
            Enums.MediaType.Other -> SectionColors(
                ContextCompat.getColor(context, R.color.section_other),
                ContextCompat.getColor(context, R.color.section_other_light),
                ContextCompat.getColor(context, R.color.section_other_dark),
                R.drawable.other_item_small
            )
            else -> {
                val typedValue = TypedValue()
                context.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
                val primary = ContextCompat.getColor(context, typedValue.resourceId)
                SectionColors(primary, primary, primary, R.drawable.ic_gallery_black_24dp)
            }
        }

        holder.binding.mediaItemCategoryStrip.setBackgroundColor(colors.main)

        if (item.Cover != null && item.Cover.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(item.Cover, 0, item.Cover.size)
            holder.binding.mediaItemImageCover.setImageBitmap(bitmap)
            holder.binding.mediaItemImageCover.imageTintList = null
        } else {
            holder.binding.mediaItemImageCover.setImageResource(colors.iconRes)
            holder.binding.mediaItemImageCover.imageTintList = ColorStateList.valueOf(colors.main)
        }

        holder.binding.textMediaStatusInfo2.backgroundTintList = ColorStateList.valueOf(colors.light)
        holder.binding.textMediaStatusInfo2.setTextColor(colors.dark)

        holder.binding.mediaItemEditItem.imageTintList = ColorStateList.valueOf(colors.main)

        val crudUtil = SaveEditDeleteUtils(item, holder)
        holder.itemView.setOnClickListener {
            crudUtil.addEditItem(true, item.Id)
        }
        holder.binding.mediaItemEditItem.setOnClickListener {
            crudUtil.addEditItem(true, item.Id)
        }
        holder.binding.mediaItemDeleteItem.setOnClickListener {
            crudUtil.showDeleteDialog()
        }
    }

    class TransformViewHolder(val binding: ItemTransformBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DisplayMediaItem>() {
            override fun areItemsTheSame(oldItem: DisplayMediaItem, newItem: DisplayMediaItem): Boolean =
                oldItem.Id == newItem.Id

            override fun areContentsTheSame(oldItem: DisplayMediaItem, newItem: DisplayMediaItem): Boolean =
                oldItem.Title == newItem.Title &&
                        oldItem.MediaType == newItem.MediaType &&
                        (oldItem.Cover?.contentEquals(newItem.Cover ?: byteArrayOf()) ?: (newItem.Cover == null))
        }
    }
}