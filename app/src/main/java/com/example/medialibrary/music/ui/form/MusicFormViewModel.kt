package com.example.medialibrary.music.ui.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import controllers.MusicController
import models.music.Music
import models.music.MusicItem
import models.music.MusicObj
import models.shared.Tag
import models.shared.Enums as SharedEnums

class MusicFormViewModel : ViewModel() {

    private val _music = MutableLiveData(Music().apply {
        MediaType = SharedEnums.MediaType.Music
    })
    val music: LiveData<Music> = _music

    private val _items = MutableLiveData<MutableList<MusicItem>>(mutableListOf())
    val items: LiveData<MutableList<MusicItem>> = _items

    private val _newTag = MutableLiveData<String>()
    val newTag: LiveData<String> = _newTag

    fun updateArtist(artist: String) {
        _music.value?.Artist = artist
    }

    fun updateTag(tag: Tag) {
        _music.value?.Tag = tag.Id
        if (tag.Id <= "0") {
            _newTag.value = tag.Name
        } else {
            _newTag.value = ""
        }
    }

    fun updateTagName(name: String) {
        _newTag.value = name
        _music.value?.Tag = "0"
    }

    fun updateCollectingPriority(priority: SharedEnums.CollectingPriority) {
        _music.value?.CollectingPriority = priority
    }

    fun addOrUpdateItem(item: MusicItem, position: Int = -1) {
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

    fun loadCd(id: String, controller: MusicController?) {
        val loadedCd = controller?.GetMusic(id)
        loadedCd?.let {
            _music.value = it
            _items.value = it.MusicItems?.toMutableList() ?: mutableListOf()
        }
    }

    fun getSaveObject(): MusicObj {
        val saveObj = MusicObj()
        val m = _music.value ?: Music()
        m.MusicItems = _items.value ?: mutableListOf()
        saveObj.Music = m
        saveObj.NewTag = _newTag.value
        return saveObj
    }

    fun validate(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val m = _music.value
        if (m == null) {
            errors["general"] = "Music data missing"
            return errors
        }
        if (m.Artist.isNullOrBlank()) {
            errors["artist"] = "Artist is required"
        }
        return errors
    }
}
