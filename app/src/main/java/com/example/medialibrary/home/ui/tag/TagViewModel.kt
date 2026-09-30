package com.example.medialibrary.home.ui.tag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.shared.Tag

class TagViewModel: ViewModel() {
    private val _tags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>> = _tags

    fun setTags(list: List<Tag>) {
        _tags.value = list
    }
}