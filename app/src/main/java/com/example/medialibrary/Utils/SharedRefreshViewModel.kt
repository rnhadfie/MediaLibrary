package com.example.medialibrary.Utils

import androidx.lifecycle.ViewModel

class SharedRefreshViewModel : ViewModel() {
    var refreshVersion = 0

    fun incrementVersion() {
        refreshVersion++
    }
}
