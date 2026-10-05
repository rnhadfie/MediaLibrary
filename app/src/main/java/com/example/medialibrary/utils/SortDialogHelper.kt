package com.example.medialibrary.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.R
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.ItemSortRowBinding
import models.shared.Filter
import models.shared.SortModel

object SortDialogHelper {

    data class SortOptionItem(
        val key: String,
        val labelResId: Int,
        var state: Boolean?
    )

    fun showSortDialog(
        context: Context,
        filter: Filter,
        isMain: Boolean = false,
        sortModel: SortModel? = null,
        onSortApplied: (Filter) -> Unit
    ) {
        val db = DialogSortContentBinding.inflate(LayoutInflater.from(context))

        val availableKeys = if (isMain) {
            listOf("Priority", "Alphabetical", "ItemMediaType")
        } else {
            listOf("Priority", "Alphabetical")
        }

        val orderKeys = (filter.SortOrder ?: emptyList())
            .filter { availableKeys.contains(it) }
            .toMutableList()

        availableKeys.forEach { key ->
            if (!orderKeys.contains(key)) {
                orderKeys.add(key)
            }
        }

        val sortItems = orderKeys.map { key ->
            when (key) {
                "Priority" -> SortOptionItem(key, R.string.priority, filter.SortPriority)
                "Alphabetical" -> SortOptionItem(key, R.string.alphabetical, filter.SortAlphabetical)
                else -> SortOptionItem(key, R.string.itemMediaType, filter.SortItemMediaType)
            }
        }.toMutableList()

        val adapter = SortAdapter(sortItems)
        db.recyclerSortOptions.layoutManager = LinearLayoutManager(context)
        db.recyclerSortOptions.adapter = adapter

        AlertDialog.Builder(context)
            .setTitle(R.string.sort)
            .setView(db.root)
            .setPositiveButton("Apply") { _, _ ->
                filter.SortOrder = sortItems.map { it.key }
                sortItems.forEach { item ->
                    when (item.key) {
                        "Priority" -> filter.SortPriority = item.state
                        "Alphabetical" -> filter.SortAlphabetical = item.state
                        "ItemMediaType" -> filter.SortItemMediaType = item.state
                    }
                }

                if (sortModel != null) {
                    sortModel.Alphabetical = filter.SortAlphabetical
                    sortModel.Priority = filter.SortPriority
                    sortModel.ItemMediaType = filter.SortItemMediaType
                    sortModel.SortOrder = ArrayList(filter.SortOrder)
                }
                onSortApplied(filter)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private class SortAdapter(
        private val items: MutableList<SortOptionItem>
    ) : RecyclerView.Adapter<SortAdapter.SortViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortViewHolder {
            val binding = ItemSortRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SortViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SortViewHolder, position: Int) {
            holder.bind(items[position], position, items.size)
        }

        override fun getItemCount(): Int = items.size

        inner class SortViewHolder(val binding: ItemSortRowBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: SortOptionItem, position: Int, totalSize: Int) {
                binding.textOrderBadge.text = "#${position + 1}"
                binding.labelText.setText(item.labelResId)

                updateButtonState(binding.triStateButton, item.state)

                val clickToggle = {
                    item.state = when (item.state) {
                        null -> true
                        true -> false
                        false -> null
                    }
                    updateButtonState(binding.triStateButton, item.state)
                }

                binding.triStateButton.setOnClickListener { clickToggle() }
                binding.labelText.setOnClickListener { clickToggle() }

                binding.btnMoveUp.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE
                binding.btnMoveDown.visibility = if (position < totalSize - 1) View.VISIBLE else View.INVISIBLE

                binding.btnMoveUp.setOnClickListener {
                    if (position > 0) {
                        val temp = items[position]
                        items[position] = items[position - 1]
                        items[position - 1] = temp
                        notifyDataSetChanged()
                    }
                }

                binding.btnMoveDown.setOnClickListener {
                    if (position < totalSize - 1) {
                        val temp = items[position]
                        items[position] = items[position + 1]
                        items[position + 1] = temp
                        notifyDataSetChanged()
                    }
                }
            }

            private fun updateButtonState(button: Button, state: Boolean?) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                when (state) {
                    null -> {
                        button.text = ""
                        button.setTextColor(Color.BLACK)
                    }
                    true -> {
                        button.setText(R.string.asc)
                        button.setTextColor(Color.BLACK)
                    }
                    false -> {
                        button.setText(R.string.desc)
                        button.setTextColor(Color.BLACK)
                    }
                }
            }
        }
    }
}
