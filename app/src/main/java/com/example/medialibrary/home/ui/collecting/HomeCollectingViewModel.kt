package com.example.medialibrary.home.ui.collecting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.shared.DisplayMediaItem

class HomeCollectingViewModel : ViewModel() {

    private val _items = MutableLiveData<List<DisplayMediaItem>>()
    val items: LiveData<List<DisplayMediaItem>> = _items

    fun setItems(itemList: List<DisplayMediaItem>) {
        _items.value = itemList
    }
}