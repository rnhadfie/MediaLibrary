package com.example.medialibrary.book.ui.utils

import androidx.lifecycle.ViewModel
import com.example.medialibrary.backend.models.book.BookFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class SortFilterViewmodel : ViewModel() {
    private val _fitler = MutableStateFlow<BookFilter?>(null)
    val currentBookFilter: StateFlow<BookFilter?> = _fitler.asStateFlow()

    fun updateBookFilter(profile: BookFilter) {
        _fitler.value = profile
    }
/*
    private val _Sort = MutableStateFlow<BookFilter?>(null)
    val currentBookFilter: StateFlow<BookFilter?> = _fitler.asStateFlow()

    fun updateBookFilter(profile: BookFilter) {
        _fitler.value = profile
    }*/
}