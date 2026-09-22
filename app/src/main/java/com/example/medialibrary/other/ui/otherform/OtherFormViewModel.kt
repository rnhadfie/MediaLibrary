package com.example.medialibrary.other.ui.otherform

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.models.other.*
import com.example.medialibrary.backend.models.shared.Enums as SharedEnums
import com.example.medialibrary.backend.models.shared.Tag

class OtherFormViewModel : ViewModel() {
    private val _other = MutableLiveData<Other>(Other().apply {
        MediaType = SharedEnums.MediaType.Other
    })
    val other: LiveData<Other> = _other

    private val _items = MutableLiveData<MutableList<OtherItem>>(mutableListOf())
    val items: LiveData<MutableList<OtherItem>> = _items

    private val _newTag = MutableLiveData<String>()
    val newTag: LiveData<String> = _newTag

    fun updateTitle(title: String) {
        _other.value?.Title = title
    }



    fun updateTag(tag: Tag) {
        _other.value?.Tag = tag.Id
        if (tag.Id <= 0) {
            _newTag.value = tag.Name
        } else {
            _newTag.value = ""
        }
    }

    fun updateTagName(name: String) {
        _newTag.value = name
        _other.value?.Tag = 0
    }

    fun updateCover(cover: ByteArray?) {
        _other.value?.Cover = cover
        _other.value = _other.value // Trigger observers
    }



    fun toggleCollecting(collecting: Boolean) {
        _other.value?.Collecting = collecting
    }

    fun toggleCompleted(completed: Boolean) {
        _other.value?.HasSeriesEnded = completed
    }

    fun toggleCollectionComplete(collected: Boolean) {
        _other.value?.HasCollectedAllItems = collected
    }


    fun addOrUpdateItem(item: OtherItem, position: Int = -1) {
        val currentList = _items.value ?: mutableListOf()
        if (position >= 0 && position < currentList.size) {
            currentList[position] = item
        } else {
            currentList.add(item)
        }
        _items.value = currentList
    }

    fun deleteItem(position: Int) {
        val currentList = _items.value ?: mutableListOf()
        if (position >= 0 && position < currentList.size) {
            currentList.removeAt(position)
            _items.value = currentList
        }
    }

    fun loadOtherCollection(id: Int, controller: OtherController?) {
        val loadedOtherCollection = controller?.GetOtherItem(id)
        loadedOtherCollection?.let {
            _other.value = it
            _items.value = it.Items?.toMutableList() ?: mutableListOf()

        }
    }

    fun getSaveObject(): OtherSaveObj {
        val saveObj = OtherSaveObj()
        saveObj.Other = _other.value
        saveObj.Other.Items = _items.value
        saveObj.NewTag = _newTag.value
        return saveObj
    }

    fun validate(): String? {
        val b = _other.value ?: return "Book data missing"
        if (b.Title.isNullOrBlank()) return "Title is required"
        return null
    }
}