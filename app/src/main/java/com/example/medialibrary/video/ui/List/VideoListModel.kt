package com.example.medialibrary.video.ui.List

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medialibrary.backend.models.shared.DisplayMediaItem

class VideoListModel : ViewModel() {

    private val _items = MutableLiveData<List<DisplayMediaItem>>()
    val items: LiveData<List<DisplayMediaItem>> = _items

    fun setItems(itemList: List<DisplayMediaItem>) {
        _items.value = itemList
    }
}