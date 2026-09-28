package com.example.medialibrary.home.ui.utils

import androidx.lifecycle.ViewModel
import com.example.medialibrary.backend.models.book.BookFilter
import com.example.medialibrary.backend.models.shared.Filter
import com.example.medialibrary.backend.models.shared.SortModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SortFilterViewmodel : ViewModel() {
    private val _filter = MutableStateFlow<Filter?>(null)
    val currentItemFilter: StateFlow<Filter?> = _filter.asStateFlow()

    fun updateItemFilter(profile: Filter) {
        _filter.value = profile
    }

    private val _mainFilter = MutableStateFlow<Filter?>(null)
    val currentMainFilter: StateFlow<Filter?> = _mainFilter.asStateFlow()

    fun updateMainFilter(profile: Filter) {
        _mainFilter.value = profile
    }

    private val _sort = MutableStateFlow<SortModel?>(null)

    fun updateSortModel(sort: SortModel) {
        _sort.value = sort
        _filter.value?.SortAlphabetical = sort.Alphabetical
        _filter.value?.SortPriority = sort.Priority
        _filter.value?.SortItemMediaType = sort.ItemMediaType
    }

    fun getOrCreateItemFilter(): Filter {
        return _filter.value ?: BookFilter().also { _filter.value = it }
    }

    fun getOrCreateMainFilter(): Filter {
        return _mainFilter.value ?: Filter().also { _mainFilter.value = it }
    }

    fun getOrCreateSortModel(): SortModel {
        return _sort.value ?: SortModel().also { _sort.value = it }
    }
}