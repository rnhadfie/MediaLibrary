package com.example.medialibrary.home.ui.graphs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.shared.*

class MainGraphViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Graphs go here"
    }
    val text: LiveData<String> = _text

    private val _mediaItems = MutableLiveData<List<DisplayMediaItem>>().apply {
        value = null
    }
    val mediaItems: LiveData<List<DisplayMediaItem>> = _mediaItems

    fun setMediaItems(items: List<DisplayMediaItem>) {
        _mediaItems.value = items
    }
}
