package com.example.medialibrary.other.ui.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.medialibrary.R
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.OtherBottomSheetBinding
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import models.shared.Filter
import models.shared.MainSetup
import models.shared.SortModel

object SharedUtils {
    fun filterSheetSetup(
        f: Filter,
        setup: MainSetup,
        sheetBinding: OtherBottomSheetBinding
    ): OtherBottomSheetBinding {
        val tags = setup.Tag

        val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
        sheetBinding.tagAutocomplete.autoCompleteLabel.setHint(R.string.tag)
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.tagAutocomplete.autocomplete,
            "Tags",
            tagOptions,
            f.IncludedTags,
            f.ExcludedTags
        )

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.standaloneOrSeriesComplete.root,
            R.string.Ongoing,
            f.Ongoing
        ) { f.Ongoing = it }

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.collected.root,
            R.string.completely_collected,
            f.Collected
        ) { f.Collected = it }

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.collecting.root,
            R.string.collecting,
            f.Collecting
        ) { f.Collecting = it }

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.anyItemsOwned.root,
            R.string.started_collecting,
            f.AnyOwned
        ) { f.AnyOwned = it }

        return sheetBinding
    }

    fun showSortDialog(
        context: Context,
        filter: Filter,
        isMain: Boolean = false,
        sortModel: SortModel? = null,
        onSortApplied: (Filter) -> Unit
    ) {
        val db = DialogSortContentBinding.inflate(LayoutInflater.from(context))

        TriStateCheckBoxHelper.setupSortTriStateCheckBox(
            db.Alphabetical.root,
            R.string.alphabetical,
            filter.SortAlphabetical
        ) { filter.SortAlphabetical = it }

        TriStateCheckBoxHelper.setupSortTriStateCheckBox(
            db.Priority.root,
            R.string.priority,
            filter.SortPriority
        ) { filter.SortPriority = it }

        if (isMain) {
            db.ItemMediaType.root.visibility = View.VISIBLE
            TriStateCheckBoxHelper.setupSortTriStateCheckBox(
                db.ItemMediaType.root,
                R.string.itemMediaType,
                filter.SortItemMediaType
            ) { filter.SortItemMediaType = it }
        } else {
            db.ItemMediaType.root.visibility = View.GONE
        }

        AlertDialog.Builder(context)
            .setTitle(R.string.sort)
            .setView(db.root)
            .setPositiveButton("Apply") { _, _ ->
                filter.SortAlphabetical = db.Alphabetical.triStateButton.tag as Boolean?
                filter.SortPriority = db.Priority.triStateButton.tag as Boolean?
                if (isMain) {
                    filter.SortItemMediaType = db.ItemMediaType.triStateButton.tag as Boolean?
                }
                if (sortModel != null) {
                    sortModel.Alphabetical = filter.SortAlphabetical
                    sortModel.Priority = filter.SortPriority
                    sortModel.ItemMediaType = filter.SortItemMediaType
                }
                onSortApplied(filter)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}