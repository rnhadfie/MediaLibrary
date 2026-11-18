package com.example.medialibrary.book.ui.shelf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.book.BookSpine
import models.shared.DisplayMediaItem

class BookShelfViewModel: ViewModel() {
    private val _items = MutableLiveData<List<BookSpine>>()
    val items: LiveData<List<BookSpine>> = _items

    fun setItems(itemList: List<BookSpine>) {
        _items.value = itemList
    }
}