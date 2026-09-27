package com.example.medialibrary.video.ui.Utils

import com.example.medialibrary.R
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import com.example.medialibrary.backend.models.video.Enums
import com.example.medialibrary.backend.models.video.VideoFilter
import com.example.medialibrary.backend.models.video.VideoSetup
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
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.dropdownSheetVideoTag,
                "Video Tags",
                videoTagOptions,
                f.IncludedVideoTags,
                f.ExcludedVideoTags
            )

            val typeOptions = setup.Types.filter { it.key != 0 }.map { FilterOption(Enums.VideoType.entries[it.key], it.value) }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.dropdownSheetTypeVideo,
                "Video Types",
                typeOptions,
                f.IncludedTypes,
                f.ExcludedTypes
            )

            val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.dropdownSheetTagVideo,
                "Tags",
                tagOptions,
                f.IncludedTags,
                f.ExcludedTags
            )

            val genreOptions = setup.Genre.filter { it.genreId != 0 }.map { FilterOption(it.genreId, it.genreName) }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sheetBinding.dropdownSheetGenreVideo,
                "Genres",
                genreOptions,
                f.IncludedGenres,
                f.ExcludedGenres
            )

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.watched?.root,
                R.string.watched,
                f.Watched
            ) { f.Watched = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.watching?.root,
                R.string.watching_label,
                f.Watching
            ) { f.Watching = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.standaloneOrSeriesComplete?.root,
                R.string.Ongoing,
                f.Ongoing
            ) { f.Ongoing = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.collected?.root,
                R.string.completely_collected,
                f.Collected
            ) { f.Collected = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.collecting?.root,
                R.string.collecting,
                f.Collecting
            ) { f.Collecting = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.anyItemsOwned?.root,
                R.string.started_collecting,
                f.AnyOwned
            ) { f.AnyOwned = it }

            return sheetBinding
        }
    }
}
