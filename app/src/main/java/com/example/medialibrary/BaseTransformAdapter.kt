package com.example.medialibrary

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.utils.SaveEditDeleteUtils
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.databinding.ItemTransformBinding

open class BaseTransformAdapter : ListAdapter<DisplayMediaItem, BaseTransformAdapter.TransformViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {
        val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransformViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.item = item
        holder.binding.executePendingBindings()

        if (item.Cover != null && item.Cover.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(item.Cover, 0, item.Cover.size)
            holder.binding.mediaItemImageCover.setImageBitmap(bitmap)
        } else {
            holder.binding.mediaItemImageCover.setImageResource(R.drawable.ic_gallery_black_24dp)
        }

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
                        (oldItem.Cover?.contentEquals(newItem.Cover ?: byteArrayOf()) ?: (newItem.Cover == null))
        }
    }
}
