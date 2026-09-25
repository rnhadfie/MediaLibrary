package com.example.medialibrary.utils

import android.view.View
import android.widget.TextView
import com.example.medialibrary.R
import com.example.medialibrary.backend.models.book.BookFilter
import com.example.medialibrary.backend.models.book.BookSetup
import com.example.medialibrary.backend.models.book.Enums as BookEnums
import com.example.medialibrary.backend.models.music.MusicFilter
import com.example.medialibrary.backend.models.music.MusicSetup
import com.example.medialibrary.backend.models.other.OtherFilter
import com.example.medialibrary.backend.models.shared.Filter
import com.example.medialibrary.backend.models.shared.MainSetup
import com.example.medialibrary.backend.models.video.VideoFilter
import com.example.medialibrary.backend.models.video.VideoSetup
import com.example.medialibrary.backend.models.video.Enums as VideoEnums
import com.google.android.material.button.MaterialButton

object FilterSummaryHelper {

    fun bindFilterSummary(
        summaryCardRoot: View?,
        filter: Filter?,
        setup: Any?,
        onClear: () -> Unit
    ) {
        if (summaryCardRoot == null) return

        val summaryTextView = summaryCardRoot.findViewById<TextView>(R.id.text_active_filter_summary)
        val clearButton = summaryCardRoot.findViewById<MaterialButton>(R.id.button_clear_active_filter)

        val summary = getFilterSummaryText(filter, setup)
        if (summary.isNotEmpty()) {
            summaryCardRoot.visibility = View.VISIBLE
            summaryTextView?.text = summary
            clearButton?.setOnClickListener { onClear() }
        } else {
            summaryCardRoot.visibility = View.GONE
        }
    }

    fun hasActiveFilters(filter: Filter?): Boolean {
        return getFilterSummaryText(filter, null).isNotEmpty()
    }

    fun getFilterSummaryText(filter: Filter?, setup: Any?): String {
        if (filter == null) return ""
        val parts = mutableListOf<String>()

        if (!filter.Search.isNullOrEmpty()) {
            parts.add("Search: \"${filter.Search}\"")
        }

        if (filter.Collecting == true) parts.add("Collecting: Yes")
        if (filter.StandaloneOrSeriesIsComplete == true) parts.add("Completed: Yes")
        if (filter.AnyOwned == true) parts.add("Started: Yes")

        when (filter) {
            is BookFilter -> {
                val bookSetup = setup as? BookSetup
                if (filter.Type != null && filter.Type != BookEnums.BookType.NoneSelected) {
                    parts.add("Type: ${filter.Type}")
                }
                formatTriStateNames(filter.IncludedPublishers, filter.ExcludedPublishers, "Publisher", parts) { id ->
                    bookSetup?.Publishers?.find { it.Id == id }?.Name ?: id.toString()
                }
                formatTriStateNames(filter.IncludedTypes, filter.ExcludedTypes, "Type", parts) { it.name }
                formatTriStateNames(filter.IncludedFormats, filter.ExcludedFormats, "Format", parts) { it.name }
                formatTriStateNames(filter.IncludedTags, filter.ExcludedTags, "Tag", parts) { id ->
                    bookSetup?.Tag?.find { it.Id == id }?.Name ?: id.toString()
                }
                formatTriStateNames(filter.IncludedGenres, filter.ExcludedGenres, "Genre", parts) { id ->
                    bookSetup?.Genre?.get(id) ?: id.toString()
                }
            }
            is VideoFilter -> {
                val videoSetup = setup as? VideoSetup
                if (filter.Type != null && filter.Type != VideoEnums.VideoType.NoneSelected) {
                    parts.add("Type: ${filter.Type}")
                }
                if (filter.VideoTag != null && filter.VideoTag != VideoEnums.VideoTag.None) {
                    parts.add("Video Tag: ${filter.VideoTag}")
                }
                formatTriStateNames(filter.IncludedVideoTags, filter.ExcludedVideoTags, "Video Tag", parts) { it.name }
                formatTriStateNames(filter.IncludedTypes, filter.ExcludedTypes, "Type", parts) { it.name }
                formatTriStateNames(filter.IncludedTags, filter.ExcludedTags, "Tag", parts) { id ->
                    videoSetup?.Tag?.find { it.Id == id }?.Name ?: id.toString()
                }
                formatTriStateNames(filter.IncludedGenres, filter.ExcludedGenres, "Genre", parts) { id ->
                    videoSetup?.Genre?.get(id) ?: id.toString()
                }
            }
            is MusicFilter -> {
                val musicSetup = setup as? MusicSetup
                formatTriStateNames(filter.IncludedTags, filter.ExcludedTags, "Tag", parts) { id ->
                    musicSetup?.Tags?.find { it.Id == id }?.Name ?: id.toString()
                }
                formatTriStateNames(filter.IncludedMusicGenres, filter.ExcludedMusicGenres, "Genre", parts) { id ->
                    musicSetup?.MusicGenre?.get(id) ?: id.toString()
                }
            }
            is OtherFilter -> {
                val mainSetup = setup as? MainSetup
                formatTriStateNames(filter.IncludedTags, filter.ExcludedTags, "Tag", parts) { id ->
                    mainSetup?.Tag?.find { it.Id == id }?.Name ?: id.toString()
                }
            }
            else -> {
                val mainSetup = setup as? MainSetup
                formatTriStateNames(filter.IncludedMediaTypes, filter.ExcludedMediaTypes, "Media", parts) { it.name }
                formatTriStateNames(filter.IncludedTags, filter.ExcludedTags, "Tag", parts) { id ->
                    mainSetup?.Tag?.find { it.Id == id }?.Name ?: id.toString()
                }
                formatTriStateNames(filter.IncludedGenres, filter.ExcludedGenres, "Genre", parts) { id ->
                    mainSetup?.Genre?.get(id) ?: id.toString()
                }
            }
        }

        return parts.joinToString(" | ")
    }

    private fun <T> formatTriStateNames(
        included: List<T>?,
        excluded: List<T>?,
        label: String,
        parts: MutableList<String>,
        nameResolver: (T) -> String
    ) {
        if (!included.isNullOrEmpty()) {
            val names = included.joinToString(", ") { nameResolver(it) }
            parts.add("$label: ✓ $names")
        }
        if (!excluded.isNullOrEmpty()) {
            val names = excluded.joinToString(", ") { nameResolver(it) }
            parts.add("$label: ✗ $names")
        }
    }
}
