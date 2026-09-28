package com.example.medialibrary.music.ui.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.medialibrary.R
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import com.example.medialibrary.backend.models.music.MusicFilter
import com.example.medialibrary.backend.models.music.MusicSetup
import com.example.medialibrary.backend.models.shared.Filter
import com.example.medialibrary.backend.models.shared.SortModel
import com.example.medialibrary.databinding.DialogSortContentBinding
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
                R.string.collecting,
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
