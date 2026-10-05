package com.example.medialibrary.book.ui.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import controllers.BookController
import models.book.Book
import models.book.BookItem
import models.book.BookSaveObject
import models.book.BookSetup
import models.book.Enums
import models.book.Publisher
import models.shared.GenreObject
import models.shared.Tag
import models.shared.Enums as SharedEnums

class BookFormViewModel : ViewModel() {

    private val _book = MutableLiveData(Book().apply {
        MediaType = SharedEnums.MediaType.Book
        Type = Enums.BookType.NoneSelected
        Genre = mutableListOf()
    })
    val book: LiveData<Book> = _book

    private val _items = MutableLiveData<MutableList<BookItem>>(mutableListOf())
    val items: LiveData<MutableList<BookItem>> = _items

    private val _selectedGenres = MutableLiveData<MutableSet<GenreObject>>(mutableSetOf())
    val selectedGenres: LiveData<MutableSet<GenreObject>> = _selectedGenres

    private val _newPublisher = MutableLiveData<String>()

    private val _newTag = MutableLiveData<String>()

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
        if (publisher.Id == "") {
            _newPublisher.value = publisher.Name
        } else {
            _newPublisher.value = ""
        }
    }

    fun updatePublisherName(name: String) {
        _newPublisher.value = name
        _book.value?.Publisher = ""
    }

    fun updateTag(tag: Tag) {
        _book.value?.Tag = tag.Id
        if (tag.Id == "") {
            _newTag.value = tag.Name
        } else {
            _newTag.value = ""
        }
    }

    fun updateTagName(name: String) {
        _newTag.value = name
        _book.value?.Tag = ""
    }



    fun updateBookType(type: Enums.BookType) {
        _book.value?.Type = type
    }

    fun updateCover(cover: ByteArray?) {
        _book.value?.Cover = cover
        _book.value = _book.value // Trigger observers
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
        _book.value?.Genre = current.map { it.genreId }.toMutableList()
    }

    fun toggleCollecting(collecting: Boolean) {
        _book.value?.Collecting = collecting
    }

    fun toggleCompleted(completed: Boolean) {
        _book.value?.Ongoing = completed
    }

    fun toggleCollectionComplete(collected: Boolean) {
        _book.value?.HasCollectedAllItems = collected
    }


    fun clearItems() {
        val currentList = _items.value ?: mutableListOf()
        currentList.clear()
        _items.value = currentList
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

    fun loadBook(id: String, controller: BookController?, setup: BookSetup) {
        val loadedBook = controller?.GetBook(id)
        loadedBook?.let { it ->
            _book.value = it
            _items.value = it.Items?.toMutableList() ?: mutableListOf()

            (it.Genre?.mapNotNull { id ->
                setup.Genre.firstOrNull  { it.genreId == id }
            }?.toMutableSet() ?: mutableSetOf()).also { _selectedGenres.value = it }

            _newPublisher.value = ""
            _newTag.value = ""

        }
    }

    fun getSaveObject(): BookSaveObject {
        val saveObj = BookSaveObject()
        saveObj.book = _book.value
        saveObj.book.Items = _items.value
        saveObj.NewTag = _newTag.value
        saveObj.NewPublisher = _newPublisher.value
        return saveObj
    }


    fun updateCollectingPriority(priority: SharedEnums.CollectingPriority) {
        _book.value?.CollectingPriority = priority
    }

    fun validateFields(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val b = _book.value
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