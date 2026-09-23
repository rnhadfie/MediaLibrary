package com.example.medialibrary.music.ui.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medialibrary.backend.models.music.*
import com.example.medialibrary.backend.models.shared.Enums as SharedEnums
import com.example.medialibrary.backend.models.shared.Tag
import com.example.medialibrary.backend.controllers.MusicController

class MusicFormViewModel : ViewModel() {
    private val _music = MutableLiveData<Music>(Music().apply {
        MediaType = SharedEnums.MediaType.Music
    })
    val music: LiveData<Music> = _music


    private val _newTag = MutableLiveData<String>()

    fun updateTitle(title: String) {
        _music.value?.Title = title
    }

    fun updateArtist(artist: String) {
        _music.value?.Artist = artist
    }

    fun updateTag(tag: Tag) {
        _music.value?.Tag = tag.Id
        if (tag.Id <= 0) {
            _newTag.value = tag.Name
        } else {
            _newTag.value = ""
        }
    }

    fun updateTagName(name: String) {
        _newTag.value = name
        _music.value?.Tag = 0
    }


    fun updateCover(cover: ByteArray?) {
        _music.value?.Cover = cover
        _music.value = _music.value // Trigger observers
    }



    fun toggleCollecting(collecting: Boolean) {
        _music.value?.Collecting = collecting
    }

    fun toggleCollectionComplete(collected: Boolean) {
        _music.value?.HasCollectedAllItems = collected
    }

    fun updateMusicGenre(type: Enums.MusicGenre) {
        _music.value?.MusicGenre = type
    }




    fun loadCd(id: Int, controller: MusicController?) {
        val loadedCd = controller?.GetMusic(id)
        loadedCd?.let {
            _music.value = it

        }
    }

    fun getSaveObject(): MusicObj {
        val saveObj = MusicObj()
        saveObj.Music = _music.value
        saveObj.NewTag = _newTag.value
        return saveObj
    }

    fun validate(): String? {
        val b = _music.value ?: return "Book data missing"
        if (b.Title.isNullOrBlank()) return "Title is required"
        return null
    }
}