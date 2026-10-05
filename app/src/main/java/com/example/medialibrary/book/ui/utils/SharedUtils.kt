package com.example.medialibrary.book.ui.utils

import android.content.Context
import com.example.medialibrary.R
import com.example.medialibrary.databinding.BookBottomSheetBinding
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.SortDialogHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import models.book.BookFilter
import models.book.BookSetup
import models.book.Enums.BookFormat
import models.book.Enums.BookType
import models.shared.Filter
import models.shared.SortModel

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
                R.string.collected,
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
            SortDialogHelper.showSortDialog(context, filter, isMain, sortModel, onSortApplied)
        }
    }
}
