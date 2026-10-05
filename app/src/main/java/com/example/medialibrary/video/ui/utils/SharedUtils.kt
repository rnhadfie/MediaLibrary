package com.example.medialibrary.video.ui.utils

import android.content.Context
import com.example.medialibrary.R
import com.example.medialibrary.databinding.VideoBottomSheetBinding
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.SortDialogHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import models.shared.Filter
import models.shared.SortModel
import models.video.Enums.VideoTag
import models.video.Enums.VideoType
import models.video.VideoFilter
import models.video.VideoSetup

class SharedUtils {

    companion object {

        fun filterSheetSetup(
            filter: VideoFilter,
            setup: VideoSetup,
            sheetBinding: VideoBottomSheetBinding
        ): VideoBottomSheetBinding {

            val typeOptions = setup.Types.filter { it.key != 0 }.map { FilterOption(VideoType.entries[it.key], it.value) }
            sheetBinding.videoType.autoCompleteLabel.setHint(R.string.video_type)
            sheetBinding.videoType.autocomplete.let {
                MultiSelectFilterHelper.setupTriStateDropdown(
                    it,
                    "Types",
                    typeOptions,
                    filter.IncludedTypes,
                    filter.ExcludedTypes
                )
            }

            val tagOptions = setup.Tag.map { FilterOption(it.Id, it.Name) }
            sheetBinding.tagFilterAutocomplete?.autoCompleteLabel?.setHint(R.string.tag)
            sheetBinding.tagFilterAutocomplete?.autocomplete?.let {
                MultiSelectFilterHelper.setupTriStateDropdown(
                    it,
                    "Tags",
                    tagOptions,
                    filter.IncludedTags,
                    filter.ExcludedTags
                )
            }

            val genreOptions = setup.Genre.filter { it.genreId != 0 }.map { FilterOption(it.genreId, it.genreName) }
            sheetBinding.genreSelect.autoCompleteLabel.setHint(R.string.genre)
            sheetBinding.genreSelect.autocomplete.let {
                MultiSelectFilterHelper.setupTriStateDropdown(
                    it,
                    "Genres",
                    genreOptions,
                    filter.IncludedGenres,
                    filter.ExcludedGenres
                )
            }

            val categoryOptions = setup.VideoTags.filter { it.key != 0 }.map { FilterOption(VideoTag.entries[it.key], it.value) }
            sheetBinding.videoCategory.autoCompleteLabel.setHint(R.string.video_tags)
            sheetBinding.videoCategory.autocomplete.let {
                MultiSelectFilterHelper.setupTriStateDropdown(
                    it,
                    "Categories",
                    categoryOptions,
                    filter.IncludedVideoTags,
                    filter.ExcludedVideoTags
                )
            }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.ongoing.root,
                R.string.Ongoing,
                filter.Ongoing
            ) { filter.Ongoing = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.collected.root,
                R.string.collected_label,
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

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.watched.root,
                R.string.watched,
                filter.Watched
            ) { filter.Watched = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.watching.root,
                R.string.watching_label,
                filter.Watching
            ) { filter.Watching = it }

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
