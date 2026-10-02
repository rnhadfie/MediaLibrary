package com.example.medialibrary.home.ui.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.medialibrary.R
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.MainBottomSheetBinding
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import models.shared.DisplayMediaItem
import models.shared.Enums
import models.shared.Filter
import models.shared.MainSetup
import models.shared.SortModel

class SharedUtils {
    companion object {
        fun filterSheetSetup(
            f: Filter,
            setup: MainSetup,
            sb: MainBottomSheetBinding
        ): MainBottomSheetBinding {
            val tags = setup.Tag

            val typeOptions = setup.MediaType.filter { it.key != 0 }.map { FilterOption(Enums.MediaType.entries[it.key], it.value) }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sb.dropdownSheetTypeBook,
                "Media Types",
                typeOptions,
                f.IncludedMediaTypes,
                f.ExcludedMediaTypes
            )

            val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sb.dropdownSheetTagBook,
                "Tags",
                tagOptions,
                f.IncludedTags,
                f.ExcludedTags
            )

            val genreOptions = setup.Genre.filter { it.genreId != 0 }.map { FilterOption(it.genreId, it.genreName) }
            MultiSelectFilterHelper.setupTriStateDropdown(
                sb.dropdownSheetGenreBook,
                "Genres",
                genreOptions,
                f.IncludedGenres,
                f.ExcludedGenres
            )

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sb.standaloneOrSeriesComplete.root,
                R.string.Ongoing,
                f.Ongoing
            ) { f.Ongoing = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sb.collected.root,
                R.string.completely_collected,
                f.Collected
            ) { f.Collected = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sb.collecting.root,
                R.string.collecting,
                f.Collecting
            ) { f.Collecting = it }

            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sb.anyItemsOwned.root,
                R.string.started_collecting,
                f.AnyOwned
            ) { f.AnyOwned = it }

            return sb
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

        fun ClipboardHelper(mediaItems: List<DisplayMediaItem>, text: String): String {
            val sortedBooks = mediaItems.filter { it.MediaType == Enums.MediaType.Book }.sortedBy { it.Title }
            val sortedVideos = mediaItems.filter { it.MediaType == Enums.MediaType.Video }.sortedBy { it.Title }
            val sortedMusics = mediaItems.filter { it.MediaType == Enums.MediaType.Music }.sortedBy { it.Title }
            val sortedOthers = mediaItems.filter { it.MediaType == Enums.MediaType.Other }.sortedBy { it.Title }
            val list = buildString {
                appendLine("Books:")
                sortedBooks.forEach { book ->
                    appendLine(book.Title)
                }
                appendLine()
                appendLine("Videos:")
                sortedVideos.forEach { video ->
                    appendLine(video.Title)
                }
                appendLine()
                appendLine("Music Collection:")
                sortedMusics.forEach { music ->
                    appendLine(music.Title)
                }
                appendLine()
                appendLine("Other Collection:")
                sortedOthers.forEach { other ->
                    appendLine(other.Title)
                }
            }
            return list
        }

    }
}