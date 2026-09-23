package com.example.medialibrary.backend.controllers;

import com.example.medialibrary.backend.Serivce.BookService;

import com.example.medialibrary.backend.models.book.BookFilter;
import com.example.medialibrary.backend.models.book.BookSaveObject;
import com.example.medialibrary.backend.models.book.BookSetup;
import com.example.medialibrary.backend.models.book.Book;
import com.example.medialibrary.backend.models.book.Publisher;
import com.example.medialibrary.backend.models.shared.DisplayMediaItem;
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper;

import java.util.List;

import kotlin.Lazy;
import kotlin.LazyKt;

public class BookController {

    public Lazy<BookService> bookSerivce;
    protected MediaLibraryDbHelper dbHelper;
    public BookController(MediaLibraryDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.bookSerivce = LazyKt.lazy(() -> new BookService(dbHelper));
    }

    public BookController() {
    }
    public List<Book> GetBooks(BookFilter filter) {
        //Get All Books
        return this.bookSerivce.getValue().GetBooks(filter);
    }

    public List<DisplayMediaItem> GetListOfBooks(BookFilter filter) {
        //Get All Books
        return this.bookSerivce.getValue().GetBookDisplayLists(filter);
    }

    public Book GetBook(int id) {

       return this.bookSerivce.getValue().GetBook(id);

    }

    public boolean AddBook(BookSaveObject book) {
        return this.bookSerivce.getValue().AddBook(book);
    }

    public boolean UpdateBook(BookSaveObject book) {
        return this.bookSerivce.getValue().EditBook(book);
    }
    public boolean DeleteBook(int id) {
        return this.bookSerivce.getValue().DeleteBook(id);
    }

    public BookSetup GetBookSetup () {
        return this.bookSerivce.getValue().GetBookSetup();
    }

    public List<Publisher> GetPublishers() {
        return this.bookSerivce.getValue().GetPublishers();
    }

    public boolean AddPublisher(Publisher publisher) {
        return this.bookSerivce.getValue().AddPublisher(publisher);
    }

    public boolean UpdatePublisher(Publisher publisher) {
        return this.bookSerivce.getValue().UpdatePublisher(publisher);
    }

    public boolean DeletePublisher(int id) {
        return this.bookSerivce.getValue().DeletePublisher(id);
    }
}
