package com.example.medialibrary.video.ui.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medialibrary.backend.controllers.VideoController
import com.example.medialibrary.backend.models.shared.Enums
import com.example.medialibrary.backend.models.shared.Tag
import com.example.medialibrary.backend.models.video.Enums.VideoTag
import com.example.medialibrary.backend.models.video.Enums.VideoType
import com.example.medialibrary.backend.models.video.Video
import com.example.medialibrary.backend.models.video.VideoItem
import com.example.medialibrary.backend.models.video.VideoSaveObject

class VideoFormViewModel : ViewModel() {

    private val _video = MutableLiveData<Video>(Video().apply {
        MediaType = Enums.MediaType.Video
        Type = VideoType.NoneSelected
        Genre = mutableListOf()
    })
    val book: LiveData<Video> = _video

    private val _items = MutableLiveData<MutableList<VideoItem>>(mutableListOf())
    val items: LiveData<MutableList<VideoItem>> = _items

    private val _selectedGenres = MutableLiveData<MutableSet<Enums.Genre>>(mutableSetOf())
    val selectedGenres: LiveData<MutableSet<Enums.Genre>> = _selectedGenres

    private val _newTag = MutableLiveData<String>()
    val newTag: LiveData<String> = _newTag

    fun updateTitle(title: String) {
        _video.value?.Title = title
    }

    fun updateTag(tag: Tag) {
        _video.value?.Tag = tag.Id
        if (tag.Id <= 0) {
            _newTag.value = tag.Name
        } else {
            _newTag.value = ""
        }
    }

    fun updateTagName(name: String) {
        _newTag.value = name
        _video.value?.Tag = 0
    }



    fun updateVideoType(type: com.example.medialibrary.backend.models.video.Enums.VideoType) {
        _video.value?.Type = type
    }

    fun updateVideoTag(vidTag: VideoTag) {
        _video.value?.VideoTag = vidTag
    }

    fun updateCover(cover: ByteArray?) {
        _video.value?.Cover = cover
        _video.value = _video.value // Trigger observers
    }

    fun toggleGenre(genre: Enums.Genre) {
        val current = _selectedGenres.value ?: mutableSetOf()
        if (current.contains(genre)) {
            current.remove(genre)
        } else {
            current.add(genre)
        }
        _selectedGenres.value = current

        // Update book genres list as integers (assuming ordinal or some mapping)
        _video.value?.Genre = current.map { it.ordinal }.toMutableList()
    }

    fun toggleCollecting(collecting: Boolean) {
        _video.value?.Collecting = collecting
    }

    fun toggleCompleted(completed: Boolean) {
        _video.value?.HasSeriesEnded = completed
    }

    fun toggleCollectionComplete(collected: Boolean) {
        _video.value?.HasCollectedAllItems = collected
    }


    fun addOrUpdateItem(item: VideoItem, position: Int = -1) {
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

    fun loadVideo(id: Int, controller: VideoController?) {
        val loadedVideo = controller?.GetVideo(id)
        loadedVideo?.let {
            _video.value = it
            _items.value = it.Items?.toMutableList() ?: mutableListOf()
            _selectedGenres.value = it.Genre?.mapNotNull { id ->
                Enums.Genre.values().getOrNull(id)
            }?.toMutableSet() ?: mutableSetOf()
        }
    }

    fun getSaveObject(): VideoSaveObject {
        val saveObj = VideoSaveObject()
        saveObj.video = _video.value
        saveObj.video.Items = _items.value
        saveObj.NewTag = _newTag.value
        return saveObj
    }

    fun validate(): String? {
        val b = _video.value ?: return "Video data missing"
        if (b.Title.isNullOrBlank()) return "Title is required"
        return null
    }

}