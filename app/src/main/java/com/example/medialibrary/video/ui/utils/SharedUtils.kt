package com.example.medialibrary.video.ui.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.medialibrary.R
import models.shared.Filter
import models.shared.SortModel
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import models.video.Enums
import models.video.VideoFilter
import models.video.VideoSetup
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.VideoBottomSheetBinding

class SharedUtils {
    companion object {
        fun filterSheetSetup(
            f: VideoFilter,
            setup: VideoSetup,
            sheetBinding: VideoBottomSheetBinding
        ): VideoBottomSheetBinding {
            val tags = setup.Tag

            val videoTagOptions = setup.VideoTags.filter { it.key != 0 }.map { FilterOption(Enums.VideoTag.entries[it.key], it.value) }
            sheetBinding.videoCategory.autoCompleteLabel.setHint(R.string.video_tags)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.videoCategory.autocomplete,
                "Video Categories",
                videoTagOptions,
                f.IncludedVideoTags,
                f.ExcludedVideoTags
            )

            val typeOptions = setup.Types.filter { it.key != 0 }.map { FilterOption(Enums.VideoType.entries[it.key], it.value) }
            sheetBinding.videoType.autoCompleteLabel.setHint(R.string.video_type)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.videoType.autocomplete,
                "Video Types",
                typeOptions,
                f.IncludedTypes,
                f.ExcludedTypes
            )

            val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
            sheetBinding.tagFilterAutocomplete?.autoCompleteLabel?.setHint(R.string.tag)
            sheetBinding.tagFilterAutocomplete?.autocomplete?.let {
                MultiSelectFilterHelper.setupTriStateDropdown(
                    it,
                    "Tags",
                    tagOptions,
                    f.IncludedTags,
                    f.ExcludedTags
                )
            }

            val genreOptions = setup.Genre.filter { it.genreId != 0 }.map { FilterOption(it.genreId, it.genreName) }
            sheetBinding.genreSelect.autoCompleteLabel.setHint(R.string.genre)
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.genreSelect.autocomplete,
                "Genres",
                genreOptions,
                f.IncludedGenres,
                f.ExcludedGenres
            )

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.watched.root,
                R.string.watched,
                f.Watched
            ) { f.Watched = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.watching.root,
                R.string.watching_label,
                f.Watching
            ) { f.Watching = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.ongoing.root,
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
}
