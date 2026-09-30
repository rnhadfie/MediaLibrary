package com.example.medialibrary.music.ui.display

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.shared.MediaItem

class MusicDisplayViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Graphs go here"
    }
    val text: LiveData<String> = _text

    private val _MediaItems = MutableLiveData<List<MediaItem>>().apply {
        value = null
    }
    val MediaItems: LiveData<List<MediaItem>> = _MediaItems

    fun setMediaItems(items: List<MediaItem>) {
        _MediaItems.value = items
    }
}