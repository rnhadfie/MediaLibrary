package com.example.medialibrary.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedRefreshViewModel : ViewModel() {
    private val _refreshVersion = MutableLiveData(0)
    val refreshVersion: LiveData<Int> = _refreshVersion

    fun incrementVersion() {
        _refreshVersion.value = (_refreshVersion.value ?: 0) + 1
    }
}
