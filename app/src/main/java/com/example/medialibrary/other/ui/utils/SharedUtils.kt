package com.example.medialibrary.other.ui.utils

import android.content.Context
import com.example.medialibrary.R
import com.example.medialibrary.databinding.OtherBottomSheetBinding
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.SortDialogHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import models.other.OtherFilter
import models.shared.Filter
import models.shared.MainSetup
import models.shared.SortModel

class SharedUtils {

    companion object {

        fun filterSheetSetup(
            filter: OtherFilter,
            setup: MainSetup,
            sheetBinding: OtherBottomSheetBinding
        ): OtherBottomSheetBinding {
        val tags = setup.Tag
        val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
        sheetBinding.tagAutocomplete.autoCompleteLabel.setHint(R.string.tag)
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.tagAutocomplete.autocomplete,
            "Tags",
            tagOptions,
            filter.IncludedTags,
            filter.ExcludedTags
        )


            TriStateCheckBoxHelper.setupTriStateCheckBox(
                sheetBinding.collected.root,
                R.string.collected,
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
