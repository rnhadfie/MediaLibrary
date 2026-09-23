package com.example.medialibrary.Utils

import android.content.Context
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class FilterOption<T>(val id: T, val name: String)

object MultiSelectFilterHelper {

    fun <T> setupTriStateDropdown(
        view: AutoCompleteTextView,
        title: String,
        options: List<FilterOption<T>>,
        includedList: MutableList<T>,
        excludedList: MutableList<T>,
        onChanged: (() -> Unit)? = null
    ) {
        view.isFocusable = false
        view.isClickable = true
        view.keyListener = null

        updateSummaryText(view, options, includedList, excludedList)

        val showDialog = {
            val context = view.context
            showTriStateDialog(
                context = context,
                title = title,
                options = options,
                includedList = includedList,
                excludedList = excludedList
            ) {
                updateSummaryText(view, options, includedList, excludedList)
                onChanged?.invoke()
            }
        }

        view.setOnClickListener { showDialog() }
    }

    fun <T> updateSummaryText(
        view: AutoCompleteTextView,
        options: List<FilterOption<T>>,
        includedList: List<T>,
        excludedList: List<T>
    ) {
        val incNames = options.filter { includedList.contains(it.id) }.map { it.name }
        val excNames = options.filter { excludedList.contains(it.id) }.map { it.name }

        val text = when {
            incNames.isEmpty() && excNames.isEmpty() -> ""
            incNames.isNotEmpty() && excNames.isEmpty() -> "Included: ${incNames.joinToString(", ")}"
            incNames.isEmpty() -> "Excluded: ${excNames.joinToString(", ")}"
            else -> "Inc: ${incNames.joinToString(", ")} | Exc: ${excNames.joinToString(", ")}"
        }
        view.setText(text, false)
    }

    private fun <T> showTriStateDialog(
        context: Context,
        title: String,
        options: List<FilterOption<T>>,
        includedList: MutableList<T>,
        excludedList: MutableList<T>,
        onDismiss: () -> Unit
    ) {
        val recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            setPadding(16, 16, 16, 16)
        }

        val adapter = TriStateAdapter(options, includedList, excludedList)
        recyclerView.adapter = adapter

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(recyclerView)
            .setPositiveButton("Done") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                onDismiss()
            }
            .show()
    }

    private class TriStateAdapter<T>(
        private val options: List<FilterOption<T>>,
        private val includedList: MutableList<T>,
        private val excludedList: MutableList<T>
    ) : RecyclerView.Adapter<TriStateAdapter.ViewHolder>() {

        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val tv = TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(32, 24, 32, 24)
                textSize = 16f
            }
            return ViewHolder(tv)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val option = options[position]
            val isInc = includedList.contains(option.id)
            val isExc = excludedList.contains(option.id)
            val context = holder.textView.context

            when {
                isInc -> {
                    holder.textView.text = "✓  ${option.name}  (Included)"
                    holder.textView.setTextColor(ContextCompat.getColor(context, R.color.filter_included))
                }
                isExc -> {
                    holder.textView.text = "✗  ${option.name}  (Excluded)"
                    holder.textView.setTextColor(ContextCompat.getColor(context, R.color.filter_excluded))
                }
                else -> {
                    holder.textView.text = "    ${option.name}"
                    holder.textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
            }

            holder.itemView.setOnClickListener {
                when {
                    isInc -> {
                        includedList.remove(option.id)
                        excludedList.add(option.id)
                    }
                    isExc -> {
                        excludedList.remove(option.id)
                    }
                    else -> {
                        includedList.add(option.id)
                    }
                }
                notifyItemChanged(position)
            }
        }

        override fun getItemCount(): Int = options.size
    }
}
