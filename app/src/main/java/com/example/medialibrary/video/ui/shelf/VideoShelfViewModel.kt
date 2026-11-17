package com.example.medialibrary.video.ui.shelf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.video.VideoSpine

class VideoShelfViewModel: ViewModel() {
    private val _items = MutableLiveData<List<VideoSpine>>()
    val items: LiveData<List<VideoSpine>> = _items

    fun setItems(itemList: List<VideoSpine>) {
        _items.value = itemList
    }
}