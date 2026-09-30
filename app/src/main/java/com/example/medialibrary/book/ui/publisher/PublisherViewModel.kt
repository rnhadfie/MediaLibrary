package com.example.medialibrary.book.ui.publisher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.book.Publisher

class PublisherViewModel : ViewModel() {

    private val _publishers = MutableLiveData<List<Publisher>>()
    val publishers: LiveData<List<Publisher>> = _publishers

    fun setPublishers(list: List<Publisher>) {
        _publishers.value = list
    }
}