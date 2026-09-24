package com.example.medialibrary.book.ui.Utils

import com.example.medialibrary.R
import com.example.medialibrary.Utils.FilterOption
import com.example.medialibrary.Utils.MultiSelectFilterHelper
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
            sheetBinding.tagAutocomplete.autoCompleteLabel.setText(R.string.tag);
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.tagAutocomplete.autocomplete,
                "Tags",
                tagOptions,
                filter.IncludedTags,
                filter.ExcludedTags
            )

            val genreOptions = setup.Genre.filter { it.key != 0 }.map { FilterOption(it.key, it.value) }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.dropdownSheetGenreBook,
                "Genres",
                genreOptions,
                filter.IncludedGenres,
                filter.ExcludedGenres
            )

            sheetBinding.switchSheetCompletedBook.isChecked = filter.StandaloneOrSeriesIsComplete ?: false
            sheetBinding.switchSheetCollectedBook.isChecked = filter.Collecting ?: false
            sheetBinding.switchSheetStartedBook.isChecked = filter.AnyOwned ?: false

            return sheetBinding
        }
    }
}