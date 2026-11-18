package com.example.medialibrary

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.databinding.ItemTransformBinding
import com.example.medialibrary.shared.CoverFrameUtils
import com.example.medialibrary.shared.ImageUtils
import com.example.medialibrary.shared.SaveEditDeleteUtils
import com.example.medialibrary.shared.ThemeUtils
import models.shared.DisplayMediaItem

open class BaseTransformAdapter : ListAdapter<DisplayMediaItem, BaseTransformAdapter.TransformViewHolder>(DIFF_CALLBACK) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {
        val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.item = item
        holder.binding.executePendingBindings()

        val context = holder.itemView.context

        val colors = ThemeUtils.getThemeColor2(context, item.MediaType)

        holder.binding.mediaItemCategoryStrip.setBackgroundColor(colors.main)

        CoverFrameUtils.applyCoverFrame(
            holder.binding.mediaItemImageCover,
            item.Cover,
            item.MediaType,
            context
        )

        holder.binding.textMediaStatusInfo2.backgroundTintList = ColorStateList.valueOf(colors.light)
        holder.binding.textMediaStatusInfo2.setTextColor(colors.dark)

        holder.binding.mediaItemViewItem?.imageTintList = ColorStateList.valueOf(colors.main)
        holder.binding.mediaItemEditItem.imageTintList = ColorStateList.valueOf(colors.main)

        val crudUtil = SaveEditDeleteUtils(item, holder)
        holder.itemView.setOnClickListener {
            crudUtil.addEditItem(isEdit = false, id = item.Id, isView = true)
        }
        holder.binding.mediaItemViewItem?.setOnClickListener {
            crudUtil.addEditItem(isEdit = false, id = item.Id, isView = true)
        }
        holder.binding.mediaItemEditItem.setOnClickListener {
            crudUtil.addEditItem(isEdit = true, id = item.Id, isView = false)
        }
        holder.binding.mediaItemDeleteItem.setOnClickListener {
            crudUtil.showDeleteDialog()
        }
        holder.binding.mediaItemImageCover.setOnClickListener {
            if (item.Cover != null && item.Cover.isNotEmpty()) {
                ImageUtils.showImagePreviewDialog(context, item.Cover, item.Title)
            }
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