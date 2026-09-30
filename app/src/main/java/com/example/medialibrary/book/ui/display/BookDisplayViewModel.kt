package com.example.medialibrary.book.ui.display

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.shared.MediaItem

class BookDisplayViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Graphs go here"
    }
    val text: LiveData<String> = _text

    private val _mediaItems = MutableLiveData<List<MediaItem>>().apply {
        value = null
    }
    val mediaItems: LiveData<List<MediaItem>> = _mediaItems

    fun setMediaItems(items: List<MediaItem>) {
        _mediaItems.value = items
    }
}