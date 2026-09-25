package com.example.medialibrary.music.ui.Utils

import com.example.medialibrary.R
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import com.example.medialibrary.backend.models.music.MusicFilter
import com.example.medialibrary.backend.models.music.MusicSetup
import com.example.medialibrary.databinding.MusicBottomSheetBinding

class SharedUtils {
    companion object {
        fun filterSheetSetup(
            filter: MusicFilter,
            setup: MusicSetup,
            sheetBinding: MusicBottomSheetBinding
        ): MusicBottomSheetBinding {
            val tags = setup.Tags

            val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
            sheetBinding.tagAutocomplete.autoCompleteLabel.setText(R.string.tag)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.tagAutocomplete.autocomplete,
                "Tags",
                tagOptions,
                filter.IncludedTags,
                filter.ExcludedTags
            )

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
                R.string.collecting,
                filter.Collecting
            ) { filter.Collecting = it }

            return sheetBinding
        }
    }
}
