package com.example.medialibrary.book.ui.utils

import com.example.medialibrary.R
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import com.example.medialibrary.backend.models.book.BookFilter
import com.example.medialibrary.backend.models.book.BookSetup
import com.example.medialibrary.backend.models.book.Enums.BookFormat
import com.example.medialibrary.backend.models.book.Enums.BookType
import com.example.medialibrary.databinding.BookBottomSheetBinding

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
            sheetBinding.publisherAutocomplete.autoCompleteLabel.setText(R.string.publisher)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.publisherAutocomplete.autocomplete,
                "Publishers",
                publisherOptions,
                filter.IncludedPublishers,
                filter.ExcludedPublishers
            )

            val typeOptions = setup.Type.filter { it.key != 0 }.map { FilterOption(BookType.entries[it.key], it.value) }
            sheetBinding.bookTypeAutocomplete.autoCompleteLabel.setText(R.string.book_type_label)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.bookTypeAutocomplete.autocomplete,
                "Book Types",
                typeOptions,
                filter.IncludedTypes,
                filter.ExcludedTypes
            )

            val formatOptions = setup.Format.filter { it.key != 0 }.map { FilterOption(BookFormat.entries[it.key], it.value) }
            sheetBinding.bookFormatAutocomplete.autoCompleteLabel.setText(R.string.format_label)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.bookFormatAutocomplete.autocomplete,
                "Formats",
                formatOptions,
                filter.IncludedFormats,
                filter.ExcludedFormats
            )

            val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
            sheetBinding.tagAutocomplete.autoCompleteLabel.setText(R.string.tag)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.tagAutocomplete.autocomplete,
                "Tags",
                tagOptions,
                filter.IncludedTags,
                filter.ExcludedTags
            )

            val genreOptions = setup.Genre.filter { it.genreId != 0 }.map { FilterOption(it.genreId, it.genreName) }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.dropdownSheetGenreBook,
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
                R.string.completed_series,
                filter.StandaloneOrSeriesIsComplete
            ) { filter.StandaloneOrSeriesIsComplete = it }

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
    }
}
