package com.example.medialibrary.video.ui.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import controllers.VideoController
import models.shared.Enums
import models.shared.GenreObject
import models.shared.Tag
import models.video.Enums.VideoTag
import models.video.Enums.VideoType
import models.video.Video
import models.video.VideoItem
import models.video.VideoSaveObject
import models.video.VideoSetup

class VideoFormViewModel : ViewModel() {

    private val _video = MutableLiveData<Video>(Video().apply {
        MediaType = Enums.MediaType.Video
        Type = VideoType.NoneSelected
        Genre = mutableListOf()
    })
    val video: LiveData<Video> = _video

    private val _items = MutableLiveData<MutableList<VideoItem>>(mutableListOf())
    val items: LiveData<MutableList<VideoItem>> = _items

    private val _selectedGenres = MutableLiveData<MutableSet<GenreObject>>(mutableSetOf())
    val selectedGenres: LiveData<MutableSet<GenreObject>> = _selectedGenres

    private val _newTag = MutableLiveData<String>()

    fun updateTitle(title: String) {
        _video.value?.Title = title
    }

    fun updateTag(tag: Tag) {
        _video.value?.Tag = tag.Id
        if (tag.Id <= "0") {
            _newTag.value = tag.Name
        } else {
            _newTag.value = ""
        }
    }

    fun updateTagName(name: String) {
        _newTag.value = name
        _video.value?.Tag = "0"
    }



    fun updateVideoType(type: VideoType) {
        _video.value?.Type = type
    }

    fun updateVideoTag(vidTag: VideoTag) {
        _video.value?.VideoTag = vidTag
    }

    fun updateCover(cover: ByteArray?) {
        _video.value?.Cover = cover
        _video.value = _video.value // Trigger observers
    }

    fun toggleGenre(genre: GenreObject) {
        val current = _selectedGenres.value ?: mutableSetOf()
        if (current.contains(genre)) {
            current.remove(genre)
        } else {
            current.add(genre)
        }
        _selectedGenres.value = current

        // Update book genres list as integers (assuming ordinal or some mapping)
        _video.value?.Genre = current.map { it.genreId }.toMutableList()
    }

    fun toggleCollecting(collecting: Boolean) {
        _video.value?.Collecting = collecting
    }

    fun toggleCompleted(completed: Boolean) {
        _video.value?.Ongoing = completed
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

    fun loadVideo(id: String, controller: VideoController?, setup: VideoSetup) {
        val loadedVideo = controller?.GetVideo(id)
        loadedVideo?.let {
            _video.value = it
            _items.value = it.Items?.toMutableList() ?: mutableListOf()
            (it.Genre?.mapNotNull { id ->
                setup.Genre.firstOrNull  { it.genreId == id }
            }?.toMutableSet() ?: mutableSetOf()).also { _selectedGenres.value = it }
        }
    }

    fun getSaveObject(): VideoSaveObject {
        val saveObj = VideoSaveObject()
        saveObj.video = _video.value
        saveObj.video.Items = _items.value
        saveObj.NewTag = _newTag.value
        return saveObj
    }

    fun updateCollectingPriority(priority: Enums.CollectingPriority) {
        _video.value?.CollectingPriority = priority
    }

    fun validate(): Map<String, String>  {
        val errors = mutableMapOf<String, String>()
        val b = _video.value
        if (b == null) {
            errors["general"] = "Book data missing"
            return errors
        }
        if (b.Title.isNullOrBlank()) {
            errors["title"] = "Title is required"
        }
        return errors
    }

}