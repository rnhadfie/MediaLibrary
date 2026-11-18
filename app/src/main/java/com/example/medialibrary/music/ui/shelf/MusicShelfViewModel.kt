package com.example.medialibrary.music.ui.shelf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.music.MusicSpine

class MusicShelfViewModel: ViewModel() {
    private val _items = MutableLiveData<List<MusicSpine>>()
    val items: LiveData<List<MusicSpine>> = _items

    fun setItems(itemList: List<MusicSpine>) {
        _items.value = itemList
    }
}