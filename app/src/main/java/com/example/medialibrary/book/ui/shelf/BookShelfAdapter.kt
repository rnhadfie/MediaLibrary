package com.example.medialibrary.book.ui.shelf

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.R
import com.example.medialibrary.shared.ThemeUtils
import com.google.android.flexbox.FlexboxLayoutManager
import models.book.BookSpine

class BookShelfAdapter(
    private val onBookClick: (BookSpine) -> Unit
) : ListAdapter<BookSpine, BookShelfAdapter.SpineViewHolder>(BookDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.book_spine_item, parent, false)

        val density = parent.context.resources.displayMetrics.density
        (view.layoutParams as? FlexboxLayoutManager.LayoutParams)?.apply {
            flexGrow = 1f
            flexShrink = 0f
            maxWidth = (110 * density).toInt()
        }

        return SpineViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SpineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container: FrameLayout = itemView.findViewById(R.id.spineContainer)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvSpineTitle)
        private val backgroundDrawable: GradientDrawable? =
            container.background?.mutate() as? GradientDrawable

        init {
            container.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookClick(getItem(position))
                }
            }
        }

        fun bind(book: BookSpine) {
            val title = book.SpineDisplayTitle.orEmpty()
            tvTitle.text = title
            val spineColor = ThemeUtils.getSpineColor(
                itemView.context,
                book.SpineColorHex,
                "BookActivity",
                bindingAdapterPosition
            )
            backgroundDrawable?.setColor(spineColor)
            tvTitle.setTextColor(ThemeUtils.getContrastingTextColor(spineColor))

            val density = itemView.resources.displayMetrics.density

            // Reset width to WRAP_CONTENT so recycled views re-measure cleanly for the new title
            (container.layoutParams as? FlexboxLayoutManager.LayoutParams)?.apply {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                flexShrink = 0f

                // Scale flexGrow by title length so longer titles claim more leftover shelf space
                // while short titles stay narrow (0.3f for <=8 chars up to 2.5f for 35+ chars)
                flexGrow = (title.length / 14f).coerceIn(0.3f, 2.5f)

                // Cap max width based on title length so a short title never balloons on an incomplete row
                val maxDp = (34f + title.length * 1.4f).coerceIn(36f, 96f)
                maxWidth = (maxDp * density).toInt()
            }

            tvTitle.requestLayout()
            container.requestLayout()
        }
    }

    private class BookDiffCallback : DiffUtil.ItemCallback<BookSpine>() {
        override fun areItemsTheSame(oldItem: BookSpine, newItem: BookSpine): Boolean = oldItem.Id == newItem.Id
        override fun areContentsTheSame(oldItem: BookSpine, newItem: BookSpine): Boolean = oldItem.SpineDisplayTitle == newItem.SpineDisplayTitle
    }

    companion object {
        // Pre-parsed once so we don't re-parse the fallback color on every scroll
        private val DEFAULT_SPINE_COLOR = "#5D4037".toColorInt()
    }
}