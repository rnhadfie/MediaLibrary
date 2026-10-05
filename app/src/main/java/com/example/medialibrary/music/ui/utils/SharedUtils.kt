package com.example.medialibrary.music.ui.utils

import android.content.Context
import com.example.medialibrary.R
import com.example.medialibrary.databinding.MusicBottomSheetBinding
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.SortDialogHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import models.music.MusicFilter
import models.music.MusicSetup
import models.shared.Filter
import models.shared.SortModel

class SharedUtils {

    companion object {

        fun filterSheetSetup(
            filter: MusicFilter,
            setup: MusicSetup,
            sheetBinding: MusicBottomSheetBinding
        ): MusicBottomSheetBinding {
            val tags = setup.Tags

            val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
            sheetBinding.tagAutocomplete.autoCompleteLabel.setHint(R.string.tag)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.tagAutocomplete.autocomplete,
                "Tags",
                tagOptions,
                filter.IncludedTags,
                filter.ExcludedTags
            )

            sheetBinding.musicGenreAutocomplete.autoCompleteLabel.setHint(R.string.music_genre)
            val genreOptions = setup.MusicGenre.filter { it.key != 0 }.map {
                FilterOption(
                    it.key,
                    it.value
                )
            }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.musicGenreAutocomplete.autocomplete,
                "Genres",
                genreOptions,
                filter.IncludedGenres,
                filter.ExcludedGenres
            )

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.collected.root,
                R.string.completely_collected,
                filter.Collected
            ) { filter.Collected = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.collecting.root,
                R.string.currently_collecting,
                filter.Collecting
            ) { filter.Collecting = it }

            return sheetBinding
        }

        fun showSortDialog(
            context: Context,
            filter: MusicFilter,
            isMain: Boolean = false,
            sortModel: SortModel? = null,
            onSortApplied: (Filter) -> Unit
        ) {
            SortDialogHelper.showSortDialog(context, filter, isMain, sortModel, onSortApplied)
        }
    }
}
