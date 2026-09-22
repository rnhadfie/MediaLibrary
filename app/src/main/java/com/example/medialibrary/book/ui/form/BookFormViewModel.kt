package com.example.medialibrary.book.ui.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medialibrary.backend.controllers.BookController
import com.example.medialibrary.backend.models.book.*
import com.example.medialibrary.backend.models.book.Enums
import com.example.medialibrary.backend.models.shared.Enums as SharedEnums
import com.example.medialibrary.backend.models.shared.Tag

class BookFormViewModel : ViewModel() {

    private val _book = MutableLiveData<Book>(Book().apply {
        MediaType = SharedEnums.MediaType.Book
        Type = Enums.BookType.NoneSelected
        Genre = mutableListOf()
    })
    val book: LiveData<Book> = _book

    private val _items = MutableLiveData<MutableList<BookItem>>(mutableListOf())
    val items: LiveData<MutableList<BookItem>> = _items

    private val _selectedGenres = MutableLiveData<MutableSet<SharedEnums.Genre>>(mutableSetOf())
    val selectedGenres: LiveData<MutableSet<SharedEnums.Genre>> = _selectedGenres

    private val _newPublisher = MutableLiveData<String>()
    val newPublisher: LiveData<String> = _newPublisher

    private val _newTag = MutableLiveData<String>()
    val newTag: LiveData<String> = _newTag

    fun updateTitle(title: String) {
        _book.value?.Title = title
    }

    fun updateAuthor(author: String) {
        _book.value?.Author = author
    }

    fun updateArtist(artist: String) {
        _book.value?.Artist = artist
    }

    fun updatePublisher(publisher: Publisher) {
        _book.value?.Publisher = publisher.Id
        if (publisher.Id <= 0) {
            _newPublisher.value = publisher.Name
        } else {
            _newPublisher.value = ""
        }
    }

    fun updatePublisherName(name: String) {
        _newPublisher.value = name
        _book.value?.Publisher = 0
    }

    fun updateTag(tag: Tag) {
        _book.value?.Tag = tag.Id
        if (tag.Id <= 0) {
            _newTag.value = tag.Name
        } else {
            _newTag.value = ""
        }
    }

    fun updateTagName(name: String) {
        _newTag.value = name
        _book.value?.Tag = 0
    }



    fun updateBookType(type: Enums.BookType) {
        _book.value?.Type = type
    }

    fun updateCover(cover: ByteArray?) {
        _book.value?.Cover = cover
        _book.value = _book.value // Trigger observers
    }

    fun toggleGenre(genre: SharedEnums.Genre) {
        val current = _selectedGenres.value ?: mutableSetOf()
        if (current.contains(genre)) {
            current.remove(genre)
        } else {
            current.add(genre)
        }
        _selectedGenres.value = current

        // Update book genres list as integers (assuming ordinal or some mapping)
        _book.value?.Genre = current.map { it.ordinal }.toMutableList()
    }

    fun toggleCollecting(collecting: Boolean) {
        _book.value?.Collecting = collecting
    }

    fun toggleCompleted(completed: Boolean) {
        _book.value?.HasSeriesEnded = completed
    }

    fun toggleCollectionComplete(collected: Boolean) {
        _book.value?.HasCollectedAllItems = collected
    }


    fun addOrUpdateItem(item: BookItem, position: Int = -1) {
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

    fun loadVideo(id: Int, controller: BookController?) {
        val loadedBook = controller?.GetBook(id)
        loadedBook?.let {
            _book.value = it
            _items.value = it.Items?.toMutableList() ?: mutableListOf()
            _selectedGenres.value = it.Genre?.mapNotNull { id ->
                SharedEnums.Genre.values().getOrNull(id)
            }?.toMutableSet() ?: mutableSetOf()
        }
    }

    fun getSaveObject(): BookSaveObject {
        val saveObj = BookSaveObject()
        saveObj.book = _book.value
        saveObj.book.Items = _items.value
        saveObj.NewTag = _newTag.value
        saveObj.NewPubliser = _newPublisher.value
        return saveObj
    }

    fun validate(): String? {
        val b = _book.value ?: return "Book data missing"
        if (b.Title.isNullOrBlank()) return "Title is required"
        return null
    }
}