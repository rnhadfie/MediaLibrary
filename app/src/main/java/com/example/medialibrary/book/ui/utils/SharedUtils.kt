package com.example.medialibrary.book.ui.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.medialibrary.R
import models.book.BookFilter
import models.book.BookSetup
import models.book.Enums.BookFormat
import models.book.Enums.BookType
import models.shared.Filter
import models.shared.SortModel
import com.example.medialibrary.databinding.BookBottomSheetBinding
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper

class SharedUtils {

    companion object {
        fun filterSheetSetup(
            filter: BookFilter,
            setup: BookSetup,
            sheetBinding: BookBottomSheetBinding
        ): BookBottomSheetBinding {
            val publishers = setup.Publishers
            val tags = setup.Tag

            val publisherOptions = publishers.map { FilterOption(it.Id, it.Name) }
            sheetBinding.pubAutocompleteInput.autoCompleteLabel.setHint(R.string.publisher)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.pubAutocompleteInput.autocomplete,
                "Publishers",
                publisherOptions,
                filter.IncludedPublishers,
                filter.ExcludedPublishers
            )

            val typeOptions = setup.Type.filter { it.key != 0 }.map { FilterOption(BookType.entries[it.key], it.value) }
            sheetBinding.bookTypeAutocomplete.autoCompleteLabel.setHint(R.string.book_type_label)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.bookTypeAutocomplete.autocomplete,
                "Book Types",
                typeOptions,
                filter.IncludedTypes,
                filter.ExcludedTypes
            )

            val formatOptions = setup.Format.filter { it.key != 0 }.map { FilterOption(BookFormat.entries[it.key], it.value) }
            sheetBinding.formatAutocomplete.autoCompleteLabel.setHint(R.string.format_label)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.formatAutocomplete.autocomplete,
                "Formats",
                formatOptions,
                filter.IncludedFormats,
                filter.ExcludedFormats
            )

            val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
            sheetBinding.tagAutocomplete.autoCompleteLabel.setHint(R.string.tag)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.tagAutocomplete.autocomplete,
                "Tags",
                tagOptions,
                filter.IncludedTags,
                filter.ExcludedTags
            )

            val genreOptions = setup.Genre.filter { it.genreId != 0 }.map { FilterOption(it.genreId, it.genreName) }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.genreAutocomplete,
                "Genres",
                genreOptions,
                filter.IncludedGenres,
                filter.ExcludedGenres
            )

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.read.root,
                R.string.read_label,
                filter.Read
            ) { filter.Read = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.reading.root,
                R.string.reading_label,
                filter.Reading
            ) { filter.Reading = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.standaloneOrSeriesComplete.root,
                R.string.Ongoing,
                filter.Ongoing
            ) { filter.Ongoing = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.collected.root,
                R.string.completely_collected,
                filter.Collected
            ) { filter.Collected = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.collecting.root,
                R.string.collecting,
                filter.Collecting
            ) { filter.Collecting = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.anyItemsOwned.root,
                R.string.started_collecting,
                filter.AnyOwned
            ) { filter.AnyOwned = it }

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
}